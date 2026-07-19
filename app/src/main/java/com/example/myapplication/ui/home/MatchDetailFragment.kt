package com.example.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.ui.game.MatchWithDetails
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MatchDetailFragment : Fragment() {

    private val viewModel: MatchViewModel by activityViewModels()
    private var matchId: String? = null

    private lateinit var tvGameName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvExpansions: TextView
    private lateinit var llResultsContainer: LinearLayout

    companion object {
        private const val ARG_MATCH_ID = "match_id"

        fun newInstance(matchId: String) = MatchDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MATCH_ID, matchId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        matchId = arguments?.getString(ARG_MATCH_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_match_detail, container, false)
        
        tvGameName = view.findViewById(R.id.tvDetailGameName)
        tvDate = view.findViewById(R.id.tvDetailDate)
        tvExpansions = view.findViewById(R.id.tvDetailGeneralRules)
        llResultsContainer = view.findViewById(R.id.llResultsContainer)

        matchId?.let { id ->
            viewModel.games.observe(viewLifecycleOwner) {
                refreshDetails()
            }
            viewModel.players.observe(viewLifecycleOwner) {
                refreshDetails()
            }
            lifecycleScope.launch {
                val details = viewModel.getMatchWithDetails(id)
                currentDetails = details
                refreshDetails()
            }
        }

        return view
    }

    private var currentDetails: MatchWithDetails? = null

    private fun refreshDetails() {
        val details = currentDetails ?: return
        if (viewModel.games.value == null || viewModel.players.value == null) return
        displayDetails(details)
    }

    private fun displayDetails(details: MatchWithDetails) {
        val match = details.match
        
        // Note: I need the game name here too. I should have included it in MatchWithDetails or fetch it separately.
        // For now, I'll use a placeholder or improve the ViewModel.
        // Since I have access to games in the ViewModel, I can find it.
        val game = viewModel.games.value?.find { it.id == match.gameId }
        tvGameName.text = game?.name ?: "Juego Desconocido"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvDate.text = sdf.format(Date(match.date))

        if (!match.usedExpansions.isNullOrEmpty()) {
            tvExpansions.visibility = View.VISIBLE
            tvExpansions.text = "Expansiones: ${match.usedExpansions.joinToString(", ")}"
        } else {
            tvExpansions.visibility = View.GONE
        }

        llResultsContainer.removeAllViews()

        if (match.isTeamGame) {
            details.teams.forEach { team ->
                val teamView = LayoutInflater.from(requireContext()).inflate(android.R.layout.simple_list_item_2, llResultsContainer, false)
                val text1 = teamView.findViewById<TextView>(android.R.id.text1)
                val text2 = teamView.findViewById<TextView>(android.R.id.text2)
                
                text1.text = "Equipo: ${team.teamName} - Puntuación: ${team.score}"
                
                val members = details.players.filter { it.teamName == team.teamName }
                val membersText = members.joinToString("\n") { p ->
                    val player = viewModel.players.value?.find { it.id == p.playerId }
                    var info = "- ${player?.name ?: "Jugador"}"
                    p.turn?.let { info += " (Turno $it)" }
                    p.playerRules?.let { info += " [$it]" }
                    info
                }
                text2.text = membersText
                llResultsContainer.addView(teamView)
            }
        } else {
            details.players.forEach { p ->
                val playerView = LayoutInflater.from(requireContext()).inflate(android.R.layout.simple_list_item_2, llResultsContainer, false)
                val text1 = playerView.findViewById<TextView>(android.R.id.text1)
                val text2 = playerView.findViewById<TextView>(android.R.id.text2)
                
                val player = viewModel.players.value?.find { it.id == p.playerId }
                text1.text = "${player?.name ?: "Jugador"} - Puntuación: ${p.score}"
                
                var info = ""
                p.turn?.let { info += "Turno $it" }
                p.playerRules?.let { info += if (info.isEmpty()) "$it" else ", $it" }
                text2.text = info
                
                llResultsContainer.addView(playerView)
            }
        }
    }
}
