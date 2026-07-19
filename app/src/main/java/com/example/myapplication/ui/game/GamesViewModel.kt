package com.example.myapplication.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Cambiamos a AndroidViewModel para tener acceso al contexto de la aplicación para Room
class GamesViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getDatabase(application).gameDao()

    // Convierte el Flow de la base de datos directamente a LiveData para la UI[cite: 11]
    val games = gameDao.getAllGames().asLiveData()

    fun addGame(game: Game) {
        viewModelScope.launch {
            gameDao.insertGame(game)
        }
    }

    fun updateGame(game: Game) {
        viewModelScope.launch {
            gameDao.updateGame(game)
        }
    }
}