package com.example.myapplication

sealed class FiltrosJugadores {
    object Juego : FiltrosJugadores()
    object Elo : FiltrosJugadores()
    object Partidas : FiltrosJugadores()
}