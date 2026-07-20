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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.FileUtils
import java.util.Locale

class PlayerDetailFragment : Fragment() {

    private val viewModel: PlayerViewModel by activityViewModels()
    private var player: Player? = null

    private lateinit var ivPlayer: ImageView
    private lateinit var tvPlayerName: TextView
    private lateinit var tvGlobalStrength: TextView
    private lateinit var btnEdit: Button
    private lateinit var rvStats: RecyclerView
    private lateinit var statsAdapter: PlayerGameStatsAdapter

    private var selectedImageUri: Uri? = null
    private var ivDialogPreview: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivDialogPreview?.visibility = View.VISIBLE
            ivDialogPreview?.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            player = it.getParcelable(ARG_PLAYER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player_detail, container, false)
        
        ivPlayer = view.findViewById(R.id.ivPlayerDetail)
        tvPlayerName = view.findViewById(R.id.tvPlayerNameDetail)
        tvGlobalStrength = view.findViewById(R.id.tvGlobalStrength)
        btnEdit = view.findViewById(R.id.btnEditPlayer)
        rvStats = view.findViewById(R.id.rvPlayerGameStats)

        setupRecyclerView()
        setupUI(view)
        observeStats()

        return view
    }

    private fun setupRecyclerView() {
        statsAdapter = PlayerGameStatsAdapter()
        rvStats.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = statsAdapter
        }
    }

    private fun observeStats() {
        player?.let { p ->
            viewModel.getPlayerStats(p.id).observe(viewLifecycleOwner) { stats ->
                statsAdapter.submitList(stats)
            }
            viewModel.getGlobalAverageStrength(p.id).observe(viewLifecycleOwner) { avg ->
                tvGlobalStrength.text = String.format(Locale.getDefault(), "Fuerza Global Media: %.0f", avg)
            }
        }
    }

    private fun setupUI(view: View) {
        player?.let { p ->
            tvPlayerName.text = p.name
            if (p.imageUri != null) {
                try {
                    ivPlayer.setImageURI(Uri.parse(p.imageUri))
                } catch (e: SecurityException) {
                    ivPlayer.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                ivPlayer.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }

        btnEdit.setOnClickListener {
            showEditPlayerDialog()
        }

        view.findViewById<Button>(R.id.btnDeletePlayer)?.setOnClickListener {
            player?.let { p ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Jugador")
                    .setMessage("¿Estás seguro de que quieres eliminar a '${p.name}'? Sus estadísticas se perderán.")
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.deletePlayer(p)
                        parentFragmentManager.popBackStack()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun showEditPlayerDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_player, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPlayerName)
        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectPlayerImage)
        ivDialogPreview = dialogView.findViewById(R.id.ivSelectedPlayerImage)
        val btnSave = dialogView.findViewById<Button>(R.id.btnAddPlayer)
        val tvTitle = dialogView.findViewById<TextView>(android.R.id.title) // We didn't give it an ID, but we can change the text of the button

        btnSave.text = "GUARDAR CAMBIOS"
        etName.setText(player?.name)
        if (player?.imageUri != null) {
            selectedImageUri = Uri.parse(player?.imageUri)
            ivDialogPreview?.visibility = View.VISIBLE
            ivDialogPreview?.setImageURI(selectedImageUri)
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_MeepleForce)
            .setTitle("Editar Jugador")
            .setView(dialogView)
            .create()

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            if (name.isBlank()) {
                etName.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            val finalUri = selectedImageUri?.let { uri ->
                if (uri.scheme == "content") {
                    FileUtils.saveImageToInternalStorage(requireContext(), uri)
                } else {
                    uri
                }
            }

            val updatedPlayer = player?.copy(
                name = name,
                imageUri = (finalUri ?: selectedImageUri)?.toString()
            )

            updatedPlayer?.let {
                viewModel.updatePlayer(it)
                player = it
                view?.let { root -> setupUI(root) } // Refresh UI
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    companion object {
        private const val ARG_PLAYER = "arg_player"

        fun newInstance(player: Player) = PlayerDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PLAYER, player)
            }
        }
    }
}
