package com.example.myapplication.ui.player

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.PlayerCategory

class PlayerCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvNombreFiltro: TextView = view.findViewById(R.id.tvNombreFiltro)

    fun render(category: PlayerCategory) {
        when (category) {
            PlayerCategory.Elo -> tvNombreFiltro.text = "Elo"
            PlayerCategory.Juego -> tvNombreFiltro.text = "Juego"
            PlayerCategory.Partidas -> tvNombreFiltro.text = "Partidas"
        }
    }
}