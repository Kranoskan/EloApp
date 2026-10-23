package com.example.myapplication.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.player.Player
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PlayerWithGameRating(
    val player: Player,
    val strength: Double
)

// Cambiamos a AndroidViewModel para tener acceso al contexto de la aplicación para Room
class GamesViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getDatabase(application).gameDao()
    private val matchDao = AppDatabase.getDatabase(application).matchDao()
    private val playerDao = AppDatabase.getDatabase(application).playerDao()

    // Convierte el Flow de la base de datos directamente a LiveData para la UI[cite: 11]
    val games = gameDao.getAllGames().asLiveData()

    fun getAttributesForGame(gameId: String): androidx.lifecycle.LiveData<List<AttributeRating>> {
        val liveData = androidx.lifecycle.MutableLiveData<List<AttributeRating>>()
        viewModelScope.launch {
            liveData.postValue(gameDao.getAttributesForGame(gameId))
        }
        return liveData
    }

    fun getMatchCountForGame(gameId: String) = matchDao.getMatchCountForGame(gameId).asLiveData()

    fun getPlayersWithRatingForGame(gameId: String): LiveData<List<PlayerWithGameRating>> {
        return combine(
            playerDao.getPlayersForGame(gameId),
            playerDao.getRatingsForGame(gameId)
        ) { players, ratings ->
            players.map { player ->
                val rating = ratings.find { it.playerId == player.id }
                val strength = rating?.strength ?: 1200.0
                PlayerWithGameRating(player, strength)
            }.sortedByDescending { it.strength }
        }.asLiveData()
    }

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

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            matchDao.deleteTeamsForGame(game.id)
            matchDao.deletePlayersForGame(game.id)
            matchDao.deleteMatchesForGame(game.id)
            gameDao.deleteAttributesForGame(game.id)
            gameDao.deleteGame(game)
        }
    }
}
