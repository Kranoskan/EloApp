package com.example.myapplication.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.ui.game.AppDatabase
import com.example.myapplication.ui.game.AttributeRating
import com.example.myapplication.ui.game.EloCalculator
import com.example.myapplication.ui.game.GamesFragment
import com.example.myapplication.ui.home.HomeFragment
import com.example.myapplication.ui.player.PlayerFragment
import com.example.myapplication.ui.player.PlayerRating
import com.example.myapplication.util.DriveServiceHelper
import com.example.myapplication.util.ZipUtils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private var driveServiceHelper: DriveServiceHelper? = null

    private val selectZipLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { restoreDatabaseFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigation.menu.getItem(position).isChecked = true
            }
        })

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.currentItem = 0
                    true
                }

                R.id.nav_player -> {
                    viewPager.currentItem = 1
                    true
                }

                R.id.nav_games -> {
                    viewPager.currentItem = 2
                    true
                }

                else -> false
            }
        }

    }

    private inner class MainPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> PlayerFragment()
                2 -> GamesFragment()
                else -> HomeFragment()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_upload -> {
                uploadDatabase()
                true
            }

            R.id.action_download -> {
                downloadDatabase()
                true
            }

            R.id.action_recalculate -> {
                recalculateData()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun uploadDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dbName = "meepleforce_database"
                val dbFile = getDatabasePath(dbName)
                val dbShm = File(dbFile.path + "-shm")
                val dbWal = File(dbFile.path + "-wal")
                
                // Incluir también las imágenes guardadas en el almacenamiento interno
                val imageFiles = filesDir.listFiles { _, name -> 
                    name.startsWith("img_") 
                }?.toList() ?: emptyList()

                val filesToZip = mutableListOf(dbFile, dbShm, dbWal)
                filesToZip.addAll(imageFiles)
                
                val zipFile = File(cacheDir, "meepleforce_backup.zip")

                // Usamos el directorio padre de filesDir (el directorio de datos de la app)
                // como base para mantener la estructura de subcarpetas databases/ y files/
                val baseDir = filesDir.parentFile
                ZipUtils.zip(filesToZip, zipFile, baseDir)

                val contentUri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "${packageName}.fileprovider",
                    zipFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    startActivity(Intent.createChooser(shareIntent, "Compartir base de datos"))
                }
            } catch (e: Exception) {
                Log.e("Upload", "Error zipping database", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al preparar backup: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun downloadDatabase() {
        selectZipLauncher.launch("application/zip")
    }

    private fun restoreDatabaseFromUri(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dbName = "meepleforce_database"
                val dbFile = getDatabasePath(dbName)
                val dbShm = File(dbFile.path + "-shm")
                val dbWal = File(dbFile.path + "-wal")
                
                // Close database before overwriting
                AppDatabase.closeDatabase()

                // Delete current files to avoid issues
                dbFile.delete()
                dbShm.delete()
                dbWal.delete()
                
                // Opcional: limpiar imágenes actuales antes de restaurar
                filesDir.listFiles { _, name -> name.startsWith("img_") }?.forEach { it.delete() }

                contentResolver.openInputStream(uri)?.use { inputStream ->
                    // Unzip en el directorio base de la aplicación para que las carpetas 
                    // databases/ y files/ se restauren en su lugar correcto
                    ZipUtils.unzip(inputStream, filesDir.parentFile!!)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Datos restaurados correctamente", Toast.LENGTH_SHORT).show()
                    recreate()
                }
            } catch (e: Exception) {
                Log.e("Download", "Error restoring database", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al restaurar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun recalculateData() {
        val database = AppDatabase.getDatabase(this)
        val matchDao = database.matchDao()
        val playerDao = database.playerDao()
        val gameDao = database.gameDao()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val allMatches = matchDao.getAllMatchesWithDetailsAsc()

                // 1. Reset everything
                playerDao.deleteAllPlayerRatings()
                gameDao.deleteAllAttributeRatings()

                val playerRatingsMap = mutableMapOf<String, PlayerRating>() // Key: playerId:gameId
                val attributeRatingsMap =
                    mutableMapOf<String, AttributeRating>() // Key: gameId:type:name

                // 2. Process matches chronologically
                for (matchWithDetails in allMatches) {
                    val game = matchWithDetails.game
                    val players = matchWithDetails.players
                    val teams = matchWithDetails.teams

                    // Prepare current ratings for this match
                    val currentRelevantPlayerRatings = players.associate { player ->
                        val key = "${player.playerId}:${game.id}"
                        player.playerId to (playerRatingsMap[key]
                            ?: PlayerRating(playerId = player.playerId, gameId = game.id))
                    }

                    // For now, EloCalculator only handles 1vs1, so attributeRatings can be empty
                    val currentRelevantAttributeRatings = mutableMapOf<String, AttributeRating>()

                    // 3. Calculate new ratings
                    val (updatedPlayerRatings, updatedAttributeRatings) = EloCalculator.calculateEloChanges(
                        game,
                        teams,
                        players,
                        currentRelevantPlayerRatings,
                        currentRelevantAttributeRatings
                    )

                    // 4. Update in-memory maps
                    updatedPlayerRatings.forEach { rating ->
                        playerRatingsMap["${rating.playerId}:${rating.gameId}"] = rating
                    }
                    updatedAttributeRatings.forEach { rating ->
                        attributeRatingsMap["${rating.gameId}:${rating.type}:${rating.name}"] =
                            rating
                    }
                }

                // 5. Save final ratings to database
                playerDao.insertRatings(playerRatingsMap.values.toList())
                gameDao.insertAttributeRatings(attributeRatingsMap.values.toList())

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Recálculo completado", Toast.LENGTH_SHORT)
                        .show()
                    recreate() // Refresh UI
                }
            } catch (e: Exception) {
                Log.e("Recalculate", "Error recalculating", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al recalcular: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}