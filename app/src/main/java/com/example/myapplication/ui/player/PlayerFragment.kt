package com.example.myapplication.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.PlayerCategory

class PlayerFragment : Fragment() {
    private val categories = listOf(
        PlayerCategory.Juego,
        PlayerCategory.Elo,
        PlayerCategory.Partidas
    )

    private val players = listOf("Ismael", "Juan", "Maria", "Pedro", "Lucia")

    private lateinit var rvPlayerCategories: RecyclerView
    private lateinit var rvPlayers: RecyclerView
    
    private lateinit var categoryAdapter: PlayerCategoryAdapter
    private lateinit var playerAdapter: PlayerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        initComponent(view)
        initUI()
        return view
    }
    
    private fun initComponent(view: View) {
        rvPlayerCategories = view.findViewById(R.id.rvPlayerCategories)
        rvPlayers = view.findViewById(R.id.rvPlayers)
    }
    
    private fun initUI() {
        categoryAdapter = PlayerCategoryAdapter(categories)
        rvPlayerCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvPlayerCategories.adapter = categoryAdapter

        playerAdapter = PlayerAdapter(players) { playerName ->
            navigateToDetail(playerName)
        }
        rvPlayers.layoutManager = LinearLayoutManager(context)
        rvPlayers.adapter = playerAdapter
    }

    private fun navigateToDetail(name: String) {
        val fragment = PlayerDetailFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}