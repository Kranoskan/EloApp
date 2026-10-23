package com.example.myapplication.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R

class PlayerGameDetailFragment : Fragment() {

    private val viewModel: PlayerViewModel by activityViewModels()
    private var playerId: String? = null
    private var gameId: String? = null

    private lateinit var graphStrength: StrengthGraphView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvMatches: TextView
    private lateinit var llTurnsContainer: LinearLayout
    private lateinit var llTeamsContainer: LinearLayout
    private lateinit var llRulesContainer: LinearLayout
    private lateinit var llExpansionsContainer: LinearLayout

    companion object {
        private const val ARG_PLAYER_ID = "arg_player_id"
        private const val ARG_GAME_ID = "arg_game_id"

        fun newInstance(playerId: String, gameId: String): PlayerGameDetailFragment {
            val fragment = PlayerGameDetailFragment()
            val args = Bundle().apply {
                putString(ARG_PLAYER_ID, playerId)
                putString(ARG_GAME_ID, gameId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playerId = it.getString(ARG_PLAYER_ID)
            gameId = it.getString(ARG_GAME_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player_game_detail, container, false)

        graphStrength = view.findViewById(R.id.graphStrength)
        tvTitle = view.findViewById(R.id.tvPlayerGameTitle)
        tvSubtitle = view.findViewById(R.id.tvPlayerGameSubtitle)
        tvMatches = view.findViewById(R.id.tvPlayerGameMatches)
        llTurnsContainer = view.findViewById(R.id.llTurnsContainer)
        llTeamsContainer = view.findViewById(R.id.llTeamsContainer)
        llRulesContainer = view.findViewById(R.id.llRulesContainer)
        llExpansionsContainer = view.findViewById(R.id.llExpansionsContainer)

        view.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (playerId != null && gameId != null) {
            viewModel.getPlayerGameDetail(playerId!!, gameId!!).observe(viewLifecycleOwner) { data ->
                if (data != null) {
                    renderData(data)
                }
            }
        }

        return view
    }

    private fun renderData(data: PlayerGameDetailData) {
        tvTitle.text = data.game.name
        tvSubtitle.text = "Estadísticas de ${data.player.name}"
        tvMatches.text = "Partidas Jugadas: ${data.matchesPlayed}"

        graphStrength.setData(data.strengthHistory)

        // Render Turns
        llTurnsContainer.removeAllViews()
        if (data.turnCounts.isNotEmpty()) {
            data.turnCounts.forEach { (turnName, count) ->
                addStatItem(llTurnsContainer, turnName, "🎲", count)
            }
        } else {
            addPlaceholderItem(llTurnsContainer, "Sin datos de turnos para este jugador.")
        }

        // Render Teams
        llTeamsContainer.removeAllViews()
        if (data.teamCounts.isNotEmpty()) {
            data.teamCounts.forEach { (teamName, count) ->
                addStatItem(llTeamsContainer, teamName, "👥", count)
            }
        } else {
            addPlaceholderItem(llTeamsContainer, "Sin datos de equipos para este jugador.")
        }

        // Render Rules
        llRulesContainer.removeAllViews()
        if (data.ruleCounts.isNotEmpty()) {
            data.ruleCounts.forEach { (ruleName, count) ->
                addStatItem(llRulesContainer, ruleName, "📜", count)
            }
        } else {
            addPlaceholderItem(llRulesContainer, "Sin datos de modificadores para este jugador.")
        }

        // Render Expansions
        llExpansionsContainer.removeAllViews()
        if (data.expansionCounts.isNotEmpty()) {
            data.expansionCounts.forEach { (expName, count) ->
                addStatItem(llExpansionsContainer, expName, "🗺️", count)
            }
        } else {
            addPlaceholderItem(llExpansionsContainer, "Sin datos de expansiones para este jugador.")
        }
    }

    private fun addStatItem(
        container: LinearLayout,
        title: String,
        icon: String,
        count: Int
    ) {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_detail_token, container, false)

        val tvIcon = itemView.findViewById<TextView>(R.id.tvTokenIcon)
        val tvName = itemView.findViewById<TextView>(R.id.tvTokenName)
        val tvStat = itemView.findViewById<TextView>(R.id.tvTokenStat)
        val btnDelete = itemView.findViewById<TextView>(R.id.btnDeleteToken)

        tvIcon.text = icon
        tvName.text = title
        tvStat.text = count.toString()
        tvStat.visibility = View.VISIBLE
        btnDelete.visibility = View.GONE

        container.addView(itemView)
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
