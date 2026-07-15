package com.example.myapplication.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.PlayerCategory

class PlayerCategoryAdapter(private val categories: List<PlayerCategory>) :
    RecyclerView.Adapter<PlayerCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filtro_jugador, parent, false)
        return PlayerCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerCategoryViewHolder, position: Int) {
        holder.render(categories[position])
    }

    override fun getItemCount() = categories.size
}