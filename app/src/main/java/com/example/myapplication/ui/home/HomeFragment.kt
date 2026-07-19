package com.example.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.game.Match
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {
    private val viewModel: MatchViewModel by activityViewModels()
    private lateinit var rvMatches: RecyclerView
    private lateinit var matchAdapter: MatchAdapter
    private lateinit var fabAddMatch: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        rvMatches = view.findViewById(R.id.rvMatches)
        fabAddMatch = view.findViewById(R.id.fabAddMatch)
        
        matchAdapter = MatchAdapter { match ->
            navigateToDetail(match)
        }
        
        rvMatches.layoutManager = LinearLayoutManager(context)
        rvMatches.adapter = matchAdapter

        viewModel.matches.observe(viewLifecycleOwner) { matches ->
            matchAdapter.updateList(matches)
        }

        fabAddMatch.setOnClickListener {
            navigateToAddMatch()
        }
        
        return view
    }

    private fun navigateToAddMatch() {
        val fragment = AddMatchFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToDetail(match: Match) {
        val fragment = MatchDetailFragment.newInstance(match.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
