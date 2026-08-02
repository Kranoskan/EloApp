package com.example.myapplication.ui.game

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.FileUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout

class GamesFragment : Fragment() {
    // CAMBIO CLAVE: Compartir el ciclo de vida con la Activity para evitar fugas de datos al navegar
    private val viewModel: GamesViewModel by activityViewModels()

    private lateinit var rvGames: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private lateinit var fabAddGame: FloatingActionButton
    private lateinit var etSearchGame: EditText

    private var allGamesList: List<Game> = emptyList()

    private var selectedImageUri: Uri? = null
    private var ivDialogPreview: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivDialogPreview?.visibility = View.VISIBLE
            ivDialogPreview?.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_games, container, false)
        rvGames = view.findViewById(R.id.rvGames)
        fabAddGame = view.findViewById(R.id.fabAddGame)
        etSearchGame = view.findViewById(R.id.etSearchGame)

        gameAdapter = GameAdapter(emptyList()) { game ->
            navigateToDetail(game)
        }

        rvGames.layoutManager = GridLayoutManager(context, 2)
        rvGames.adapter = gameAdapter

        viewModel.games.observe(viewLifecycleOwner) { games ->
            allGamesList = games
            filterGames(etSearchGame.text.toString())
        }

        etSearchGame.addTextChangedListener { text ->
            filterGames(text.toString())
        }

        fabAddGame.setOnClickListener {
            showAddGameDialog()
        }

        return view
    }

    private fun filterGames(query: String) {
        val filteredList = if (query.isEmpty()) {
            allGamesList
        } else {
            allGamesList.filter { it.name.contains(query, ignoreCase = true) }
        }
        gameAdapter.updateList(filteredList)
    }

    private fun showAddGameDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_game, null)
        val etName = dialogView.findViewById<EditText>(R.id.etGameName)
        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectImage)
        ivDialogPreview = dialogView.findViewById(R.id.ivSelectedGameImage)
        val etMaxPlayers = dialogView.findViewById<EditText>(R.id.etMaxPlayers)
        val etLastTurn = dialogView.findViewById<EditText>(R.id.etLastTurn)
        val cbHasTeams = dialogView.findViewById<CheckBox>(R.id.cbHasTeams)
        val tilTeams = dialogView.findViewById<TextInputLayout>(R.id.tilTeams)
        val etTeams = dialogView.findViewById<EditText>(R.id.etTeams)
        val etExpansions = dialogView.findViewById<EditText>(R.id.etExpansions)
        val etSpecialRules = dialogView.findViewById<EditText>(R.id.etSpecialRules)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddGame)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_MeepleForce)
            .setView(dialogView)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        cbHasTeams.setOnCheckedChangeListener { _, isChecked ->
            tilTeams.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString()
            if (name.isBlank()) {
                etName.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            val savedUri = selectedImageUri?.let { FileUtils.saveImageToInternalStorage(requireContext(), it) }

            val game = Game(
                name = name,
                imageUri = savedUri?.toString() ?: selectedImageUri?.toString(),
                maxPlayers = etMaxPlayers.text.toString().toIntOrNull(),
                lastTurn = etLastTurn.text.toString().toIntOrNull(),
                teams = if (cbHasTeams.isChecked) etTeams.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() } else null,
                expansions = etExpansions.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                specialRules = etSpecialRules.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            )

            viewModel.addGame(game)
            selectedImageUri = null
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun navigateToDetail(game: Game) {
        val fragment = GameDetailFragment.newInstance(game)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}