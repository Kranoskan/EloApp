package com.example.myapplication.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class GamesFragment : Fragment() {
    private val games = listOf("Ajedrez", "Catan", "Monopoly", "Risk", "Dixit", "Carcassonne")
    private lateinit var rvGames: RecyclerView
    private lateinit var gameAdapter: GameAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_games, container, false)
        rvGames = view.findViewById(R.id.rvGames)
        
        gameAdapter = GameAdapter(games) { gameName ->
            navigateToDetail(gameName)
        }
        
        rvGames.layoutManager = LinearLayoutManager(context)
        rvGames.adapter = gameAdapter
        
        return view
    }

    private fun navigateToDetail(gameName: String) {
        val fragment = GameDetailFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}