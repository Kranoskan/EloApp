package com.example.myapplication.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class MatchAdapter(
    private val matches: List<String>,
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.render(matches[position], onItemSelected)
    }

    override fun getItemCount() = matches.size
}

class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvMatchGame: TextView = view.findViewById(R.id.tvMatchGame)
    private val tvMatchDate: TextView = view.findViewById(R.id.tvMatchDate)

    fun render(match: String, onItemSelected: (String) -> Unit) {
        tvMatchGame.text = match
        tvMatchDate.text = ""
        itemView.setOnClickListener { onItemSelected(match) }
    }
}