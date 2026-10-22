package com.example.myapplication.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.player.Player
import com.example.myapplication.ui.player.PlayerRating
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Cambiamos a AndroidViewModel para tener acceso al contexto de la aplicación para Room
class GamesViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getDatabase(application).gameDao()
    private val matchDao = AppDatabase.getDatabase(application).matchDao()
    private val playerDao = AppDatabase.getDatabase(application).playerDao()

    data class PlayerRatingWithPlayer(
        val player: Player,
        val rating: PlayerRating
    )

    // Convierte el Flow de la base de datos directamente a LiveData para la UI[cite: 11]
    val games = gameDao.getAllGames().asLiveData()

    fun getPlayerRatingsForGame(gameId: String): LiveData<List<PlayerRatingWithPlayer>> {
        return combine(
            playerDao.getRatingsForGame(gameId),
            playerDao.getAllPlayers()
        ) { ratings, players ->
            ratings.mapNotNull { rating ->
                val player = players.find { it.id == rating.playerId }
                if (player != null) {
                    PlayerRatingWithPlayer(player, rating)
                } else null
            }.sortedByDescending { it.rating.strength }
        }.asLiveData()
    }

    fun getAttributesForGame(gameId: String): androidx.lifecycle.LiveData<List<AttributeRating>> {
        val liveData = androidx.lifecycle.MutableLiveData<List<AttributeRating>>()
        viewModelScope.launch {
            liveData.postValue(gameDao.getAttributesForGame(gameId))
        }
        return liveData
    }

    fun getMatchCountForGame(gameId: String) = matchDao.getMatchCountForGame(gameId).asLiveData()

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