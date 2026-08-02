package com.example.myapplication.ui.player

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
import com.example.myapplication.ui.game.GamesViewModel
import com.example.myapplication.ui.game.Game
import com.example.myapplication.util.FileUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class PlayerFragment : Fragment() {

    private val viewModel: PlayerViewModel by activityViewModels()
    private val gamesViewModel: GamesViewModel by activityViewModels()

    private lateinit var rvPlayers: RecyclerView
    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var fabAddPlayer: FloatingActionButton
    private lateinit var etSearchPlayer: EditText
    private lateinit var actvFilterByGame: AutoCompleteTextView

    private var allPlayersWithStrength: List<PlayerWithStrength> = emptyList()
    private var allRatings: List<PlayerRating> = emptyList()
    private var selectedGameId: String? = null

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
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        initComponent(view)
        initUI()
        return view
    }
    
    private fun initComponent(view: View) {
        rvPlayers = view.findViewById(R.id.rvPlayers)
        fabAddPlayer = view.findViewById(R.id.fabAddPlayer)
        etSearchPlayer = view.findViewById(R.id.etSearchPlayer)
        actvFilterByGame = view.findViewById(R.id.actvFilterByGame)
    }
    
    private fun initUI() {
        playerAdapter = PlayerAdapter(emptyList()) { player ->
            navigateToDetail(player)
        }
        rvPlayers.layoutManager = GridLayoutManager(context, 2)
        rvPlayers.adapter = playerAdapter

        viewModel.playersWithStrength.observe(viewLifecycleOwner) { playersList ->
            allPlayersWithStrength = playersList
            filterPlayers()
        }

        // We also need all ratings to filter by game played
        viewModel.allRatings.observe(viewLifecycleOwner) { ratings ->
            allRatings = ratings
            filterPlayers()
        }

        gamesViewModel.games.observe(viewLifecycleOwner) { games ->
            setupGameFilter(games)
        }

        etSearchPlayer.addTextChangedListener { filterPlayers() }

        fabAddPlayer.setOnClickListener {
            showAddPlayerDialog()
        }
    }

    private fun setupGameFilter(games: List<Game>) {
        val gameNames = mutableListOf("Todos los juegos")
        gameNames.addAll(games.map { it.name })
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, gameNames)
        actvFilterByGame.setAdapter(adapter)

        actvFilterByGame.setOnItemClickListener { _, _, position, _ ->
            selectedGameId = if (position == 0) {
                null
            } else {
                games[position - 1].id
            }
            filterPlayers()
        }
    }

    private fun filterPlayers() {
        val query = etSearchPlayer.text.toString()
        
        var filteredList = allPlayersWithStrength

        // Filter by name
        if (query.isNotEmpty()) {
            filteredList = filteredList.filter { it.player.name.contains(query, ignoreCase = true) }
        }

        // Filter by game played
        if (selectedGameId != null) {
            val playersWhoPlayedGame = allRatings.filter { it.gameId == selectedGameId }.map { it.playerId }.toSet()
            filteredList = filteredList.filter { playersWhoPlayedGame.contains(it.player.id) }
        }

        playerAdapter.updateList(filteredList)
    }

    private fun showAddPlayerDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_player, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPlayerName)
        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectPlayerImage)
        val ivDialogPreview = dialogView.findViewById<ImageView>(R.id.ivSelectedPlayerImage)
        this.ivDialogPreview = ivDialogPreview
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddPlayer)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_MeepleForce)
            .setView(dialogView)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
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

            val player = Player(
                name = name,
                imageUri = (savedUri ?: selectedImageUri)?.toString()
            )

            viewModel.addPlayer(player)
            selectedImageUri = null
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun navigateToDetail(player: Player) {
        val fragment = PlayerDetailFragment.newInstance(player)
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
