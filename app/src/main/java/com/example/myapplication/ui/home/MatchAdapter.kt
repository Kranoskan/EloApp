package com.example.myapplication.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.game.Match
import com.example.myapplication.ui.game.MatchWithGame
import java.text.SimpleDateFormat
import java.util.*

class MatchAdapter(
    private var matchItems: List<MatchItem> = emptyList(),
    private val onItemSelected: (Match) -> Unit
) : RecyclerView.Adapter<MatchViewHolder>() {

    data class MatchItem(
        val data: MatchWithGame,
        val number: Int
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.render(matchItems[position], onItemSelected)
    }

    override fun getItemCount() = matchItems.size

    fun updateList(newList: List<MatchWithGame>) {
        // Ordenar por fecha ascendente para asignar números
        val sortedAsc = newList.sortedBy { it.match.date }
        val gameCounts = mutableMapOf<String, Int>()
        
        val items = sortedAsc.map { matchWithGame ->
            val gameId = matchWithGame.game.id
            val count = gameCounts.getOrDefault(gameId, 0) + 1
            gameCounts[gameId] = count
            MatchItem(matchWithGame, count)
        }
        
        // Volver a ordenar descendente para el historial (más reciente primero)
        matchItems = items.sortedByDescending { it.data.match.date }
        notifyDataSetChanged()
    }
}

class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvMatchGame: TextView = view.findViewById(R.id.tvMatchGame)
    private val tvMatchDate: TextView = view.findViewById(R.id.tvMatchDate)
    private val ivMatchGame: ImageView = view.findViewById(R.id.ivMatchGame)

    fun render(matchItem: MatchAdapter.MatchItem, onItemSelected: (Match) -> Unit) {
        val match = matchItem.data.match
        val game = matchItem.data.game
        
        tvMatchGame.text = "${game.name} ${matchItem.number}"
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvMatchDate.text = sdf.format(Date(match.date))

        if (game.imageUri != null) {
            try {
                ivMatchGame.setImageURI(android.net.Uri.parse(game.imageUri))
                ivMatchGame.colorFilter = null
                ivMatchGame.setPadding(0, 0, 0, 0)
            } catch (e: SecurityException) {
                ivMatchGame.setImageResource(R.drawable.ic_meeple)
            }
        } else {
            ivMatchGame.setImageResource(R.drawable.ic_meeple)
        }

        itemView.setOnClickListener { onItemSelected(match) }
    }
}
