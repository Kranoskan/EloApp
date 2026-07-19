package com.example.myapplication.ui.game

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R

class GameDetailFragment : Fragment() {

    private val viewModel: GamesViewModel by activityViewModels()
    private lateinit var currentGame: Game

    // Vistas globales del fragmento
    private lateinit var tvTitle: TextView
    private lateinit var ivImage: ImageView
    private lateinit var tvMaxPlayers: TextView
    private lateinit var llTurnsContainer: LinearLayout
    private lateinit var tvTurnsHeader: TextView
    private lateinit var llTeamsContainer: LinearLayout
    private lateinit var tvTeamsHeader: TextView
    private lateinit var llRulesContainer: LinearLayout
    private lateinit var tvRulesHeader: TextView

    // Contratos de galería para cambiar la foto desde el detalle
    private val changeImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val updatedGame = currentGame.copy(imageUri = it.toString())
            updateAndRefresh(updatedGame)
        }
    }

    companion object {
        private const val ARG_GAME = "arg_game"

        fun newInstance(game: Game): GameDetailFragment {
            val fragment = GameDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_GAME, game)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game_detail, container, false)

        // Recuperar el juego inicial enviado por argumento
        currentGame = arguments?.getParcelable<Game>(ARG_GAME) ?: return view

        // Vincular vistas
        tvTitle = view.findViewById(R.id.tvGameTitleDetail)
        ivImage = view.findViewById(R.id.ivGameDetailImage)
        tvMaxPlayers = view.findViewById(R.id.tvMaxPlayersDetail)
        llTurnsContainer = view.findViewById(R.id.llTurnsContainer)
        tvTurnsHeader = view.findViewById(R.id.tvTurnsHeader)
        llTeamsContainer = view.findViewById(R.id.llTeamsContainer)
        tvTeamsHeader = view.findViewById(R.id.tvTeamsHeader)
        llRulesContainer = view.findViewById(R.id.llRulesContainer)
        tvRulesHeader = view.findViewById(R.id.tvRulesHeader)

        setupListeners()
        renderGameData(currentGame)

        return view
    }

    private fun setupListeners() {
        // 1. Cambiar imagen al pulsar sobre ella
        ivImage.setOnClickListener {
            changeImageLauncher.launch("image/*")
        }

        // 2. Modificar número máximo de jugadores y turnos al pulsar sobre la loseta de jugadores
        tvMaxPlayers.setOnClickListener {
            showEditPlayersAndTurnsDialog()
        }

        // 3. Modificar Equipos al mantener pulsado o hacer click en el encabezado
        tvTeamsHeader.setOnClickListener {
            showAddElementDialog("Equipo") { newTeam ->
                val currentTeams = currentGame.teams?.toMutableList() ?: mutableListOf()
                currentTeams.add(newTeam)
                updateAndRefresh(currentGame.copy(teams = currentTeams))
            }
        }

        // 4. Modificar Reglas al hacer click en su encabezado
        tvRulesHeader.setOnClickListener {
            showAddElementDialog("Regla Especial") { newRule ->
                val currentRules = currentGame.specialRules?.toMutableList() ?: mutableListOf()
                currentRules.add(newRule)
                updateAndRefresh(currentGame.copy(specialRules = currentRules))
            }
        }
    }

    private fun renderGameData(game: Game) {
        tvTitle.text = game.name

        if (game.imageUri != null) {
            ivImage.setImageURI(Uri.parse(game.imageUri))
            ivImage.colorFilter = null
            ivImage.setPadding(0, 0, 0, 0)
        } else {
            ivImage.setImageResource(R.drawable.ic_meeple)
        }

        val maxPlayersText = game.maxPlayers?.toString() ?: "Sin especificar"
        tvMaxPlayers.text = "Máx. Jugadores: $maxPlayersText (Toca para editar)"

        llTurnsContainer.removeAllViews()
        llTeamsContainer.removeAllViews()
        llRulesContainer.removeAllViews()

        // 1. Render de Turnos con la nueva UI
        val lastTurn = game.lastTurn ?: 0
        tvTurnsHeader.visibility = View.VISIBLE
        if (lastTurn > 0) {
            for (i in 1..lastTurn) {
                addInfoItem(llTurnsContainer, "Turno $i", "🎲", isDeletable = false) {}
            }
        } else {
            addPlaceholderItem(llTurnsContainer, "Sin límites de turnos especificados.")
        }

        // 2. Render de Facciones / Equipos
        tvTeamsHeader.visibility = View.VISIBLE
        val teams = game.teams ?: emptyList()
        if (teams.isNotEmpty()) {
            teams.forEach { teamName ->
                addInfoItem(llTeamsContainer, teamName, "👥", isDeletable = true) {
                    val updatedTeams = teams.toMutableList().apply { remove(teamName) }
                    updateAndRefresh(currentGame.copy(teams = if (updatedTeams.isEmpty()) null else updatedTeams))
                }
            }
        } else {
            addPlaceholderItem(llTeamsContainer, "No hay equipos. (Pulsa el encabezado para añadir)")
        }

        // 3. Render de Reglas Especiales
        tvRulesHeader.visibility = View.VISIBLE
        val rules = game.specialRules ?: emptyList()
        if (rules.isNotEmpty()) {
            rules.forEach { rule ->
                addInfoItem(llRulesContainer, rule, "📜", isDeletable = true) {
                    val updatedRules = rules.toMutableList().apply { remove(rule) }
                    updateAndRefresh(currentGame.copy(specialRules = if (updatedRules.isEmpty()) null else updatedRules))
                }
            }
        } else {
            addPlaceholderItem(llRulesContainer, "Sin reglas modificadas. (Pulsa el encabezado para añadir)")
        }
    }

    // Encapsula la actualización del ViewModel y refresca la UI actual
    private fun updateAndRefresh(updatedGame: Game) {
        currentGame = updatedGame
        viewModel.updateGame(updatedGame)
        renderGameData(updatedGame)
        Toast.makeText(context, "Componentes actualizados en el tablero", Toast.LENGTH_SHORT).show()
    }

    private fun addInfoItem(
        container: LinearLayout,
        title: String,
        icon: String,
        isDeletable: Boolean,
        onDeleted: () -> Unit
    ) {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_detail_token, container, false)

        val tvIcon = itemView.findViewById<TextView>(R.id.tvTokenIcon)
        val tvName = itemView.findViewById<TextView>(R.id.tvTokenName)
        val btnDelete = itemView.findViewById<TextView>(R.id.btnDeleteToken)

        tvIcon.text = icon
        tvName.text = title

        if (isDeletable) {
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Componente")
                    .setMessage("¿Quieres retirar esto de la partida?")
                    .setPositiveButton("Sí") { _, _ -> onDeleted() }
                    .setNegativeButton("No", null)
                    .show()
            }
        } else {
            btnDelete.visibility = View.GONE
        }

        container.addView(itemView)
    }

    // Diálogo dinámico para añadir texto (Reglas/Equipos)
    private fun showAddElementDialog(elementName: String, onElementAdded: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            hint = "Escribe el nombre del/de la $elementName"
            setSingleLine()
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Añadir $elementName")
            .setView(input)
            .setPositiveButton("Añadir") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) onElementAdded(text)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Diálogo dinámico para editar jugadores y turnos máximos
    private fun showEditPlayersAndTurnsDialog() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }
        val etPlayers = EditText(context).apply {
            hint = "Número máximo de jugadores"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(currentGame.maxPlayers?.toString() ?: "")
        }
        val etTurns = EditText(context).apply {
            hint = "Último turno posible"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(currentGame.lastTurn?.toString() ?: "")
        }
        layout.addView(etPlayers)
        layout.addView(etTurns)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Parámetros de Partida")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val updatedGame = currentGame.copy(
                    maxPlayers = etPlayers.text.toString().toIntOrNull(),
                    lastTurn = etTurns.text.toString().toIntOrNull()
                )
                updateAndRefresh(updatedGame)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addPlaceholderItem(container: LinearLayout, text: String) {
        val textView = TextView(context).apply {
            this.text = text
            this.textSize = 14f
            this.setPadding(24, 16, 24, 16)
            val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorSecondary))
            setTextColor(typedArray.getColor(0, resources.getColor(android.R.color.darker_gray, null)))
            typedArray.recycle()
        }
        container.addView(textView)
    }
}