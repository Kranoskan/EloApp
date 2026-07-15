package com.example.myapplication.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class PlayerAdapter(
    private val players: List<String>,
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.render(players[position], onItemSelected)
    }

    override fun getItemCount() = players.size
}

class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvPlayerName: TextView = view.findViewById(R.id.tvPlayerName)

    fun render(player: String, onItemSelected: (String) -> Unit) {
        tvPlayerName.text = player
        itemView.setOnClickListener { onItemSelected(player) }
    }
}