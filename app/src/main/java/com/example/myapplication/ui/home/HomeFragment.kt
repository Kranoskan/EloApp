package com.example.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class HomeFragment : Fragment() {
    private val matches = listOf("Ajedrez", "Catan", "Monopoly", "Risk", "Dixit")
    private lateinit var rvMatches: RecyclerView
    private lateinit var matchAdapter: MatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        rvMatches = view.findViewById(R.id.rvMatches)
        
        matchAdapter = MatchAdapter(matches) { matchName ->
            navigateToDetail(matchName)
        }
        
        rvMatches.layoutManager = LinearLayoutManager(context)
        rvMatches.adapter = matchAdapter
        
        return view
    }

    private fun navigateToDetail(matchName: String) {
        val fragment = MatchDetailFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}