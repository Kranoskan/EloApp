package com.example.myapplication.ui.player

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlayerFragment : Fragment() {

    private val viewModel: PlayerViewModel by activityViewModels()

    private lateinit var rvPlayers: RecyclerView
    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var fabAddPlayer: FloatingActionButton

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
    }
    
    private fun initUI() {
        playerAdapter = PlayerAdapter(emptyList()) { player ->
            navigateToDetail(player)
        }
        rvPlayers.layoutManager = GridLayoutManager(context, 2)
        rvPlayers.adapter = playerAdapter

        viewModel.players.observe(viewLifecycleOwner) { playersList: List<Player> ->
            playerAdapter.updateList(playersList)
        }

        fabAddPlayer.setOnClickListener {
            showAddPlayerDialog()
        }
    }

    private fun showAddPlayerDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_player, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPlayerName)
        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectPlayerImage)
        ivDialogPreview = dialogView.findViewById(R.id.ivSelectedPlayerImage)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddPlayer)

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_MeepleForce)
            .setView(dialogView)
            .create()

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString()
            if (name.isBlank()) {
                etName.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            val player = Player(
                name = name,
                imageUri = selectedImageUri?.toString()
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
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
