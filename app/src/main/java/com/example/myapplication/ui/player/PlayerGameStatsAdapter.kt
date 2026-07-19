package com.example.myapplication.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.util.Locale

class PlayerGameStatsAdapter : RecyclerView.Adapter<PlayerGameStatsAdapter.ViewHolder>() {

    private var stats: List<PlayerGameStats> = emptyList()

    fun submitList(newStats: List<PlayerGameStats>) {
        stats = newStats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_game_stats, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount(): Int = stats.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvGameName: TextView = view.findViewById(R.id.tvStatGameName)
        private val tvStrength: TextView = view.findViewById(R.id.tvStatStrength)
        private val tvWinProb: TextView = view.findViewById(R.id.tvStatWinProb)
        private val tvMatches: TextView = view.findViewById(R.id.tvStatMatches)

        fun bind(stat: PlayerGameStats) {
            tvGameName.text = stat.gameName
            tvStrength.text = "Fuerza: ${stat.strength}"
            tvWinProb.text = String.format(Locale.getDefault(), "Prob. Victoria: %.0f%%", stat.winProbability * 100)
            tvMatches.text = "Partidas: ${stat.matchesPlayed}"
        }
    }
}
