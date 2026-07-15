package com.example.myapplication.ui.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class GameAdapter(
    private val games: List<String>,
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.render(games[position], onItemSelected)
    }

    override fun getItemCount() = games.size
}

class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvGameTitle: TextView = view.findViewById(R.id.tvGameTitle)

    fun render(game: String, onItemSelected: (String) -> Unit) {
        tvGameTitle.text = game
        itemView.setOnClickListener { onItemSelected(game) }
    }
}