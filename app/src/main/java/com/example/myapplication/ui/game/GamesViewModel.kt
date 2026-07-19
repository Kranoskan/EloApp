package com.example.myapplication.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GamesViewModel : ViewModel() {
    private val _games = MutableLiveData<List<Game>>(emptyList())
    val games: LiveData<List<Game>> = _games

    fun addGame(game: Game) {
        val currentList = _games.value ?: emptyList()
        _games.value = currentList + game
    }
}