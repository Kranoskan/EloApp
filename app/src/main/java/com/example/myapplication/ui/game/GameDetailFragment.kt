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
import com.example.myapplication.util.FileUtils

class GameDetailFragment : Fragment() {

    private val viewModel: GamesViewModel by activityViewModels()
    private lateinit var currentGame: Game

    // Vistas globales del fragmento
    private lateinit var tvTitle: TextView
    private lateinit var ivImage: ImageView
    private lateinit var tvMaxPlayers: TextView
    private lateinit var tvTotalMatches: TextView
    private lateinit var llTurnsContainer: LinearLayout
    private lateinit var tvTurnsHeader: TextView
    private lateinit var llTeamsContainer: LinearLayout
    private lateinit var tvTeamsHeader: TextView
    private lateinit var llRulesContainer: LinearLayout
    private lateinit var tvRulesHeader: TextView
    private lateinit var llExpansionsContainer: LinearLayout
    private lateinit var tvExpansionsHeader: TextView

    // Contratos de galería para cambiar la foto desde el detalle
    private val changeImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedUri = FileUtils.saveImageToInternalStorage(requireContext(), it)
            val updatedGame = currentGame.copy(imageUri = (savedUri ?: it).toString())
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
        tvTotalMatches = view.findViewById(R.id.tvTotalMatchesDetail)
        llTurnsContainer = view.findViewById(R.id.llTurnsContainer)
        tvTurnsHeader = view.findViewById(R.id.tvTurnsHeader)
        llTeamsContainer = view.findViewById(R.id.llTeamsContainer)
        tvTeamsHeader = view.findViewById(R.id.tvTeamsHeader)
        llRulesContainer = view.findViewById(R.id.llRulesContainer)
        tvRulesHeader = view.findViewById(R.id.tvRulesHeader)
        llExpansionsContainer = view.findViewById(R.id.llExpansionsContainer)
        tvExpansionsHeader = view.findViewById(R.id.tvExpansionsHeader)

        setupListeners(view)
        
        viewModel.getAttributesForGame(currentGame.id).observe(viewLifecycleOwner) { ratings ->
            renderGameData(currentGame, ratings)
        }

        observeMatchCount()

        return view
    }

    private fun observeMatchCount() {
        viewModel.getMatchCountForGame(currentGame.id).observe(viewLifecycleOwner) { count ->
            tvTotalMatches.text = "Total Partidas: $count"
        }
    }

    private fun setupListeners(view: View) {
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

        // 5. Modificar Expansiones al hacer click en su encabezado
        tvExpansionsHeader.setOnClickListener {
            showAddElementDialog("Expansión/Mapa") { newExpansion ->
                val currentExpansions = currentGame.expansions?.toMutableList() ?: mutableListOf()
                currentExpansions.add(newExpansion)
                updateAndRefresh(currentGame.copy(expansions = currentExpansions))
            }
        }

        // 6. Eliminar juego
        view.findViewById<Button>(R.id.btnDeleteGame)?.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Juego")
                .setMessage("¿Estás seguro de que quieres eliminar '${currentGame.name}'? Esto borrará también todas sus partidas.")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.deleteGame(currentGame)
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun renderGameData(game: Game, attributeRatings: List<AttributeRating> = emptyList()) {
        tvTitle.text = game.name

        if (game.imageUri != null) {
            try {
                ivImage.setImageURI(Uri.parse(game.imageUri))
                ivImage.colorFilter = null
                ivImage.setPadding(0, 0, 0, 0)
            } catch (e: SecurityException) {
                ivImage.setImageResource(R.drawable.ic_meeple)
            }
        } else {
            ivImage.setImageResource(R.drawable.ic_meeple)
        }

        val maxPlayersText = game.maxPlayers?.toString() ?: "Sin especificar"
        tvMaxPlayers.text = "Máx. Jugadores: $maxPlayersText (Toca para editar)"

        llTurnsContainer.removeAllViews()
        llTeamsContainer.removeAllViews()
        llRulesContainer.removeAllViews()
        llExpansionsContainer.removeAllViews()

        // 1. Render de Turnos con la nueva UI
        val lastTurn = game.lastTurn ?: 0
        tvTurnsHeader.visibility = View.VISIBLE
        if (lastTurn > 0) {
            for (i in 1..lastTurn) {
                val turnName = "Turno $i"
                val rating = attributeRatings.find { it.type == "TURN" && it.name == turnName }?.strength ?: 0.0
                addInfoItem(llTurnsContainer, turnName, "🎲", isDeletable = false, strength = rating) {}
            }
        } else {
            addPlaceholderItem(llTurnsContainer, "Sin límites de turnos especificados.")
        }

        // 2. Render de Facciones / Equipos
        tvTeamsHeader.visibility = View.VISIBLE
        val teams = game.teams ?: emptyList()
        if (teams.isNotEmpty()) {
            teams.forEach { teamName ->
                val rating = attributeRatings.find { it.type == "TEAM" && it.name == teamName }?.strength ?: 0.0
                addInfoItem(llTeamsContainer, teamName, "👥", isDeletable = true, strength = rating) {
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
                val rating = attributeRatings.find { it.type == "RULE" && it.name == rule }?.strength ?: 0.0
                addInfoItem(llRulesContainer, rule, "📜", isDeletable = true, strength = rating) {
                    val updatedRules = rules.toMutableList().apply { remove(rule) }
                    updateAndRefresh(currentGame.copy(specialRules = if (updatedRules.isEmpty()) null else updatedRules))
                }
            }
        } else {
            addPlaceholderItem(llRulesContainer, "Sin reglas asimetricas ni roles. (Pulsa el encabezado para añadir)")
        }

        // 4. Render de Expansiones y Mapas
        tvExpansionsHeader.visibility = View.VISIBLE
        val expansions = game.expansions ?: emptyList()
        if (expansions.isNotEmpty()) {
            expansions.forEach { expansion ->
                addInfoItem(llExpansionsContainer, expansion, "🗺️", isDeletable = true, showStat = false) {
                    val updatedExpansions = expansions.toMutableList().apply { remove(expansion) }
                    updateAndRefresh(currentGame.copy(expansions = if (updatedExpansions.isEmpty()) null else updatedExpansions))
                }
            }
        } else {
            addPlaceholderItem(llExpansionsContainer, "Sin expansiones ni mapas. (Pulsa el encabezado para añadir)")
        }
    }

    // Encapsula la actualización del ViewModel y refresca la UI actual
    private fun updateAndRefresh(updatedGame: Game) {
        currentGame = updatedGame
        viewModel.updateGame(updatedGame)
        viewModel.getAttributesForGame(updatedGame.id).observe(viewLifecycleOwner) { ratings ->
            renderGameData(updatedGame, ratings)
        }
        Toast.makeText(context, "Componentes actualizados en el tablero", Toast.LENGTH_SHORT).show()
    }

    private fun addInfoItem(
        container: LinearLayout,
        title: String,
        icon: String,
        isDeletable: Boolean,
        strength: Double = 0.0,
        showStat: Boolean = true,
        onDeleted: () -> Unit
    ) {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_detail_token, container, false)

        val tvIcon = itemView.findViewById<TextView>(R.id.tvTokenIcon)
        val tvName = itemView.findViewById<TextView>(R.id.tvTokenName)
        val tvStat = itemView.findViewById<TextView>(R.id.tvTokenStat)
        val btnDelete = itemView.findViewById<TextView>(R.id.btnDeleteToken)

        tvIcon.text = icon
        tvName.text = title

        if (showStat) {
            // Mostrar fuerza del atributo (calculada o extraída del texto)
            val extraStrength = extractStrength(title)
            val totalStrength = strength + extraStrength
            tvStat.text = "Fuerza ${totalStrength.toInt()}"
            tvStat.visibility = View.VISIBLE
        } else {
            tvStat.visibility = View.GONE
        }

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

    private fun extractStrength(text: String): Double {
        val regex = Regex("""\(([+-]?\d+)\)""")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.toDouble() ?: 0.0
    }
}