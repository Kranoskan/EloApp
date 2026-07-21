package com.example.myapplication.ui.player

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.util.Locale

class PlayerAdapter(
    private var players: List<PlayerWithStrength>,
    private val onItemSelected: (Player) -> Unit
) : RecyclerView.Adapter<PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.render(players[position], onItemSelected)
    }

    override fun getItemCount() = players.size

    fun updateList(newList: List<PlayerWithStrength>) {
        players = newList
        notifyDataSetChanged()
    }
}

class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvPlayerName: TextView = view.findViewById(R.id.tvPlayerName)
    private val ivPlayer: ImageView = view.findViewById(R.id.ivPlayer)
    private val tvPlayerStrength: TextView = view.findViewById(R.id.tvPlayerStrength)

    fun render(item: PlayerWithStrength, onItemSelected: (Player) -> Unit) {
        val player = item.player
        tvPlayerName.text = player.name
        tvPlayerStrength.text = String.format(Locale.getDefault(), "Fuerza: %.0f", item.averageStrength)

        if (player.imageUri != null) {
            try {
                ivPlayer.setImageURI(Uri.parse(player.imageUri))
            } catch (e: SecurityException) {
                ivPlayer.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            ivPlayer.setImageResource(R.drawable.ic_launcher_foreground) // Default image
        }
        itemView.setOnClickListener { onItemSelected(player) }
    }
}
