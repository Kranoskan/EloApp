package com.example.myapplication

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FiltrosViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val tvNombreFiltro: TextView = view.findViewById(R.id.tvNombreFiltro)

    fun render (categorias: FiltrosJugadores){
        tvNombreFiltro.text = "ejemplo"
    }
}