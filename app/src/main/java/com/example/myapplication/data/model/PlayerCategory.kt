package com.example.myapplication.data.model

sealed class PlayerCategory {
    object Juego : PlayerCategory()
    object Elo : PlayerCategory()
    object Partidas : PlayerCategory()
}