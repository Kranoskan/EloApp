package com.example.myapplication.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.game.Match
import com.example.myapplication.ui.game.MatchWithGame
import java.text.SimpleDateFormat
import java.util.*

class MatchAdapter(
    private var matches: List<MatchWithGame>,
    private val onItemSelected: (Match) -> Unit
) : RecyclerView.Adapter<MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.render(matches[position], onItemSelected)
    }

    override fun getItemCount() = matches.size

    fun updateList(newList: List<MatchWithGame>) {
        matches = newList
        notifyDataSetChanged()
    }
}

class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvMatchGame: TextView = view.findViewById(R.id.tvMatchGame)
    private val tvMatchDate: TextView = view.findViewById(R.id.tvMatchDate)

    fun render(matchWithGame: MatchWithGame, onItemSelected: (Match) -> Unit) {
        val match = matchWithGame.match
        val game = matchWithGame.game
        
        tvMatchGame.text = game.name
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvMatchDate.text = sdf.format(Date(match.date))

        itemView.setOnClickListener { onItemSelected(match) }
    }
}
