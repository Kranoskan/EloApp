package com.example.myapplication.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.R
import com.example.myapplication.ui.home.HomeFragment
import com.example.myapplication.ui.player.PlayerFragment
import com.example.myapplication.ui.game.GamesFragment
import com.example.myapplication.ui.game.AppDatabase
import com.example.myapplication.util.DriveServiceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private var driveServiceHelper: DriveServiceHelper? = null

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleSignInResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_player -> {
                    loadFragment(PlayerFragment())
                    true
                }
                R.id.nav_games -> {
                    loadFragment(GamesFragment())
                    true
                }
                else -> false
            }
        }
        
        silentSignIn()
    }

    private fun silentSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        client.silentSignIn()
            .addOnSuccessListener { googleAccount ->
                handleSignInAccount(googleAccount)
            }
            .addOnFailureListener { e ->
                Log.d("DriveInfo", "Silent sign-in failed: ${e.message}")
            }
    }

    private fun requestSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    private fun handleSignInResult(result: Intent?) {
        if (result == null) {
            Log.e("DriveError", "Sign in result is null")
            return
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                handleSignInAccount(account)
            } else {
                Toast.makeText(this, "Google account is null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                10 -> "Error 10: Error de configuración (¿SHA-1 registrado?)"
                12500 -> "Error 12500: Fallo en la configuración de Google Play Services"
                12501 -> "Inicio de sesión cancelado por el usuario"
                7 -> "Error 7: Error de red"
                else -> "Error de inicio de sesión: ${e.statusCode}"
            }
            Log.e("DriveError", "Sign in failed: ${e.statusCode}", e)
            if (e.statusCode != 12501) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleSignInAccount(googleAccount: com.google.android.gms.auth.api.signin.GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = googleAccount.account

        val googleDriveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("EloBoard")
            .build()

        driveServiceHelper = DriveServiceHelper(googleDriveService)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun uploadDatabase() {
        if (driveServiceHelper == null) {
            requestSignIn()
            return
        }

        val dbFile = getDatabasePath("meepleforce_database")
        if (dbFile.exists()) {
            Thread {
                val fileId = driveServiceHelper?.uploadFile(dbFile, "meepleforce_database.db")
                runOnUiThread {
                    if (fileId != null) {
                        Toast.makeText(this, "Copia de seguridad subida con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al subir la copia de seguridad", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    private fun downloadDatabase() {
        if (driveServiceHelper == null) {
            requestSignIn()
            return
        }

        val dbFile = getDatabasePath("meepleforce_database")
        Thread {
            // Close database before overwriting
            AppDatabase.closeDatabase()
            
            // Delete journal files if they exist
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            val success = driveServiceHelper?.downloadFile(dbFile, "meepleforce_database.db") ?: false
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Datos cargados con éxito. Reiniciando aplicación...", Toast.LENGTH_SHORT).show()
                    // Restart app or reload data
                    recreate()
                } else {
                    Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}