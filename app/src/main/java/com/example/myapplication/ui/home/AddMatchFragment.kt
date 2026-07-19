package com.example.myapplication.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.example.myapplication.ui.game.*
import com.example.myapplication.ui.player.Player
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class AddMatchFragment : Fragment() {

    private val viewModel: MatchViewModel by activityViewModels()
    
    private lateinit var actvSelectGame: AutoCompleteTextView
    private lateinit var tvExpansionsLabel: TextView
    private lateinit var cgExpansions: ChipGroup
    private lateinit var btnSelectExpansions: Button
    private lateinit var swIsTeamGame: SwitchMaterial
    private lateinit var llParticipantsContainer: LinearLayout
    private lateinit var btnAddParticipant: Button
    private lateinit var btnSaveMatch: Button

    private var allGames: List<Game> = emptyList()
    private var allPlayers: List<Player> = emptyList()
    private var selectedGame: Game? = null
    private var selectedExpansions: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_match, container, false)
        
        actvSelectGame = view.findViewById(R.id.actvSelectGame)
        tvExpansionsLabel = view.findViewById(R.id.tvExpansionsLabel)
        cgExpansions = view.findViewById(R.id.cgExpansions)
        btnSelectExpansions = view.findViewById(R.id.btnSelectExpansions)
        swIsTeamGame = view.findViewById(R.id.swIsTeamGame)
        llParticipantsContainer = view.findViewById(R.id.llParticipantsContainer)
        btnAddParticipant = view.findViewById(R.id.btnAddParticipant)
        btnSaveMatch = view.findViewById(R.id.btnSaveMatch)

        viewModel.games.observe(viewLifecycleOwner) { games ->
            allGames = games
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, games.map { it.name })
            (actvSelectGame as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
        }

        viewModel.players.observe(viewLifecycleOwner) { players ->
            allPlayers = players
        }

        actvSelectGame.setOnItemClickListener { _, _, position, _ ->
            selectedGame = allGames[position]
            onGameSelected()
        }

        btnSelectExpansions.setOnClickListener {
            showExpansionsDialog()
        }

        swIsTeamGame.setOnCheckedChangeListener { _, _ ->
            llParticipantsContainer.removeAllViews()
        }

        btnAddParticipant.setOnClickListener {
            if (swIsTeamGame.isChecked) {
                addTeamView()
            } else {
                addPlayerView(llParticipantsContainer)
            }
        }

        btnSaveMatch.setOnClickListener {
            saveMatch()
        }

        return view
    }

    private fun onGameSelected() {
        val game = selectedGame ?: return
        
        // Setup expansions UI
        if (!game.expansions.isNullOrEmpty()) {
            tvExpansionsLabel.visibility = View.VISIBLE
            btnSelectExpansions.visibility = View.VISIBLE
            cgExpansions.visibility = View.VISIBLE
            selectedExpansions.clear()
            updateExpansionsChips()
        } else {
            tvExpansionsLabel.visibility = View.GONE
            btnSelectExpansions.visibility = View.GONE
            cgExpansions.visibility = View.GONE
        }
        
        // Reset participants if game changes
        llParticipantsContainer.removeAllViews()
    }

    private fun showExpansionsDialog() {
        val game = selectedGame ?: return
        val expansions = game.expansions?.toTypedArray() ?: return
        val checkedItems = expansions.map { selectedExpansions.contains(it) }.toBooleanArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar Expansiones")
            .setMultiChoiceItems(expansions, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedExpansions.add(expansions[which])
                } else {
                    selectedExpansions.remove(expansions[which])
                }
            }
            .setPositiveButton("Aceptar") { _, _ ->
                updateExpansionsChips()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateExpansionsChips() {
        cgExpansions.removeAllViews()
        selectedExpansions.forEach { expansion ->
            val chip = Chip(requireContext())
            chip.text = expansion
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                selectedExpansions.remove(expansion)
                updateExpansionsChips()
            }
            cgExpansions.addView(chip)
        }
    }

    private fun addTeamView() {
        val teamView = LayoutInflater.from(requireContext()).inflate(R.layout.item_add_team, llParticipantsContainer, false)
        val actvTeamName = teamView.findViewById<AutoCompleteTextView>(R.id.actvTeamName)
        val btnRemoveTeam = teamView.findViewById<ImageButton>(R.id.btnRemoveTeam)
        val btnAddPlayerToTeam = teamView.findViewById<Button>(R.id.btnAddPlayerToTeam)
        val llTeamPlayersContainer = teamView.findViewById<LinearLayout>(R.id.llTeamPlayersContainer)

        val teams = selectedGame?.teams ?: emptyList()
        if (teams.isNotEmpty()) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, teams)
            actvTeamName.setAdapter(adapter)
            // Make it a dropdown
            actvTeamName.inputType = android.text.InputType.TYPE_NULL
        }

        btnRemoveTeam.setOnClickListener { llParticipantsContainer.removeView(teamView) }
        btnAddPlayerToTeam.setOnClickListener { addPlayerView(llTeamPlayersContainer, isInsideTeam = true) }

        llParticipantsContainer.addView(teamView)
    }

    private fun addPlayerView(container: LinearLayout, isInsideTeam: Boolean = false) {
        val playerView = LayoutInflater.from(requireContext()).inflate(R.layout.item_add_player, container, false)
        val actvPlayerName = playerView.findViewById<AutoCompleteTextView>(R.id.actvPlayerName)
        val btnRemovePlayer = playerView.findViewById<ImageButton>(R.id.btnRemovePlayer)
        val etScore = playerView.findViewById<EditText>(R.id.etPlayerScore)

        if (isInsideTeam) {
            playerView.findViewById<View>(R.id.tilPlayerScore)?.let { it.visibility = View.GONE }
        }

        // Players dropdown
        val playerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allPlayers.map { it.name })
        actvPlayerName.setAdapter(playerAdapter)

        // Special Rules multi-select
        val selectedPlayerRules = mutableListOf<String>()
        playerView.tag = selectedPlayerRules
        val btnSelectPlayerRules = playerView.findViewById<Button>(R.id.btnSelectPlayerRules)
        val cgPlayerRules = playerView.findViewById<ChipGroup>(R.id.cgPlayerRules)

        btnSelectPlayerRules.setOnClickListener {
            val rules = selectedGame?.specialRules?.toTypedArray() ?: return@setOnClickListener
            if (rules.isEmpty()) return@setOnClickListener

            val checkedItems = rules.map { selectedPlayerRules.contains(it) }.toBooleanArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar Reglas/Roles")
                .setMultiChoiceItems(rules, checkedItems) { _, which, isChecked ->
                    if (isChecked) {
                        selectedPlayerRules.add(rules[which])
                    } else {
                        selectedPlayerRules.remove(rules[which])
                    }
                }
                .setPositiveButton("Aceptar") { _, _ ->
                    updatePlayerRulesChips(cgPlayerRules, selectedPlayerRules)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnRemovePlayer.setOnClickListener { container.removeView(playerView) }

        container.addView(playerView)
    }

    private fun updatePlayerRulesChips(chipGroup: ChipGroup, selectedRules: MutableList<String>) {
        chipGroup.removeAllViews()
        selectedRules.forEach { rule ->
            val chip = Chip(requireContext())
            chip.text = rule
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                selectedRules.remove(rule)
                updatePlayerRulesChips(chipGroup, selectedRules)
            }
            chipGroup.addView(chip)
        }
    }

    private fun saveMatch() {
        val game = selectedGame ?: run {
            Toast.makeText(requireContext(), "Selecciona un juego", Toast.LENGTH_SHORT).show()
            return
        }

        val match = Match(
            gameId = game.id,
            usedExpansions = if (selectedExpansions.isNotEmpty()) selectedExpansions.toList() else null,
            isTeamGame = swIsTeamGame.isChecked
        )

        val matchTeams = mutableListOf<MatchTeam>()
        val matchPlayers = mutableListOf<MatchPlayer>()

        if (swIsTeamGame.isChecked) {
            for (i in 0 until llParticipantsContainer.childCount) {
                val teamView = llParticipantsContainer.getChildAt(i)
                val teamName = teamView.findViewById<AutoCompleteTextView>(R.id.actvTeamName).text.toString()
                val score = teamView.findViewById<EditText>(R.id.etTeamScore).text.toString().toIntOrNull() ?: 0
                
                if (teamName.isBlank()) continue

                matchTeams.add(MatchTeam(matchId = match.id, teamName = teamName, score = score))

                val playersContainer = teamView.findViewById<LinearLayout>(R.id.llTeamPlayersContainer)
                for (j in 0 until playersContainer.childCount) {
                    val playerView = playersContainer.getChildAt(j)
                    val playerName = playerView.findViewById<AutoCompleteTextView>(R.id.actvPlayerName).text.toString()
                    val player = allPlayers.find { it.name == playerName }
                    
                    if (player != null) {
                        val turn = playerView.findViewById<EditText>(R.id.etPlayerTurn).text.toString().toIntOrNull()
                        val rules = playerView.tag as? List<String>
                        
                        matchPlayers.add(MatchPlayer(
                            matchId = match.id,
                            playerId = player.id,
                            teamName = teamName,
                            turn = turn,
                            playerRules = if (!rules.isNullOrEmpty()) rules else null
                        ))
                    }
                }
            }
        } else {
            for (i in 0 until llParticipantsContainer.childCount) {
                val playerView = llParticipantsContainer.getChildAt(i)
                val playerName = playerView.findViewById<AutoCompleteTextView>(R.id.actvPlayerName).text.toString()
                val player = allPlayers.find { it.name == playerName }
                
                if (player != null) {
                    val score = playerView.findViewById<EditText>(R.id.etPlayerScore).text.toString().toIntOrNull() ?: 0
                    val turn = playerView.findViewById<EditText>(R.id.etPlayerTurn).text.toString().toIntOrNull()
                    val rules = playerView.tag as? List<String>
                    
                    matchPlayers.add(MatchPlayer(
                        matchId = match.id,
                        playerId = player.id,
                        score = score,
                        turn = turn,
                        playerRules = if (!rules.isNullOrEmpty()) rules else null
                    ))
                }
            }
        }

        if (matchPlayers.isEmpty()) {
            Toast.makeText(requireContext(), "Añade al menos un jugador", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addMatch(match, matchTeams, matchPlayers)
        parentFragmentManager.popBackStack()
    }
}
