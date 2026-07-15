package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class FiltrosJugadorAdapter(private val categorias: List<FiltrosJugadores>):
    RecyclerView.Adapter<FiltrosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiltrosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filtro_jugador, parent, false)
        return FiltrosViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FiltrosViewHolder,
        position: Int
    ) {
        holder.render(categorias[position])
    }

    override fun getItemCount() = categorias.size

}
