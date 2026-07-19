package com.example.myapplication.ui.game

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class GameDetailFragment : Fragment() {

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
        
        val game = arguments?.getParcelable<Game>(ARG_GAME)
        
        val tvTitle = view.findViewById<TextView>(R.id.tvGameTitleDetail)
        val ivImage = view.findViewById<ImageView>(R.id.ivGameDetailImage)
        val tvMaxPlayers = view.findViewById<TextView>(R.id.tvMaxPlayersDetail)
        
        val llTurnsContainer = view.findViewById<LinearLayout>(R.id.llTurnsContainer)
        val tvTurnsHeader = view.findViewById<TextView>(R.id.tvTurnsHeader)
        
        val llTeamsContainer = view.findViewById<LinearLayout>(R.id.llTeamsContainer)
        val tvTeamsHeader = view.findViewById<TextView>(R.id.tvTeamsHeader)
        
        val llRulesContainer = view.findViewById<LinearLayout>(R.id.llRulesContainer)
        val tvRulesHeader = view.findViewById<TextView>(R.id.tvRulesHeader)
        
        game?.let {
            tvTitle.text = it.name
            if (it.imageUri != null) {
                ivImage.setImageURI(Uri.parse(it.imageUri))
                ivImage.colorFilter = null
                ivImage.setPadding(0, 0, 0, 0)
            }
            
            val maxPlayersText = it.maxPlayers?.toString() ?: "Sin especificar"
            tvMaxPlayers.text = "Máx. Jugadores: $maxPlayersText"

            // Populate Turns
            val lastTurn = it.lastTurn ?: 0
            if (lastTurn > 0) {
                tvTurnsHeader.visibility = View.VISIBLE
                for (i in 1..lastTurn) {
                    addInfoItem(llTurnsContainer, "Turno $i Fuerza 0")
                }
            }

            // Populate Teams
            it.teams?.let { teams ->
                if (teams.isNotEmpty()) {
                    tvTeamsHeader.visibility = View.VISIBLE
                    teams.forEach { teamName ->
                        addInfoItem(llTeamsContainer, "$teamName Fuerza 0")
                    }
                }
            }

            // Populate Special Rules
            it.specialRules?.let { rules ->
                if (rules.isNotEmpty()) {
                    tvRulesHeader.visibility = View.VISIBLE
                    rules.forEach { rule ->
                        addInfoItem(llRulesContainer, rule)
                    }
                }
            }
        }
        
        return view
    }

    private fun addInfoItem(container: LinearLayout, text: String) {
        val textView = TextView(context).apply {
            this.text = text
            this.textSize = 16f
            
            val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorSecondary))
            val color = typedArray.getColor(0, resources.getColor(android.R.color.darker_gray, null))
            this.setTextColor(color)
            typedArray.recycle()
            
            this.setPadding(0, 8, 0, 8)
        }
        container.addView(textView)
    }
}