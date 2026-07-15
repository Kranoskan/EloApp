package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class PeopleFragment : Fragment() {
    private val filtros = listOf(
        FiltrosJugadores.Juego,
        FiltrosJugadores.Elo,
        FiltrosJugadores.Partidas
    )

    private lateinit var rvFiltros: RecyclerView
    private lateinit var filtrosAdapter: FiltrosJugadorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_people, container, false)
        initComponent(view)
        initUI()
        return view
    }
    
    private fun initComponent(view: View) {
        rvFiltros = view.findViewById(R.id.rvfiltros)
    }
    
    private fun initUI(){
        filtrosAdapter = FiltrosJugadorAdapter(filtros)
        rvFiltros.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFiltros.adapter = filtrosAdapter
    }
}