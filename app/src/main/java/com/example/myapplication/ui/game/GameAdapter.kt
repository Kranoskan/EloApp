package com.example.myapplication.ui.game

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class GameAdapter(
    private var games: List<Game>,
    private val onItemSelected: (Game) -> Unit
) : RecyclerView.Adapter<GameViewHolder>() {

    fun updateList(newList: List<Game>) {
        games = newList
        notifyDataSetChanged()
    }

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
    private val ivGameImage: ImageView = view.findViewById(R.id.ivGameImage)

    fun render(game: Game, onItemSelected: (Game) -> Unit) {
        tvGameTitle.text = game.name
        if (game.imageUri != null) {
            ivGameImage.setImageURI(Uri.parse(game.imageUri))
            ivGameImage.setPadding(0, 0, 0, 0)
            ivGameImage.colorFilter = null
        } else {
            ivGameImage.setImageResource(R.drawable.ic_meeple)
            val padding = 8
            ivGameImage.setPadding(padding, padding, padding, padding)
        }

        itemView.setOnClickListener { onItemSelected(game) }
    }
}