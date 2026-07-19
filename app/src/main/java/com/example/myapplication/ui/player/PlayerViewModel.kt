package com.example.myapplication.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.game.AppDatabase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class PlayerGameStats(
    val gameName: String,
    val strength: Int = 1200,
    val winProbability: Double = 0.5,
    val matchesPlayed: Int
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playerDao = AppDatabase.getDatabase(application).playerDao()
    private val matchDao = AppDatabase.getDatabase(application).matchDao()

    val players = playerDao.getAllPlayers().asLiveData()

    fun getPlayerStats(playerId: String): LiveData<List<PlayerGameStats>> {
        return combine(
            matchDao.getMatchesForPlayer(playerId),
            playerDao.getRatingsForPlayer(playerId)
        ) { matches, ratings ->
            matches.groupBy { it.game.id }.map { (gameId, matchWithGames) ->
                val rating = ratings.find { it.gameId == gameId }
                PlayerGameStats(
                    gameName = matchWithGames.first().game.name,
                    matchesPlayed = rating?.matchesPlayed ?: matchWithGames.size,
                    strength = rating?.strength?.toInt() ?: 1200,
                    winProbability = 0.5 // TODO: Calculate this based on some average opponent?
                )
            }
        }.asLiveData()
    }

    fun getGlobalAverageStrength(playerId: String): LiveData<Double> {
        return getPlayerStats(playerId).map { stats ->
            if (stats.isEmpty()) 0.0 else stats.map { it.strength.toDouble() }.average()
        }
    }

    fun addPlayer(player: Player) {
        viewModelScope.launch {
            playerDao.insertPlayer(player)
        }
    }

    fun updatePlayer(player: Player) {
        viewModelScope.launch {
            playerDao.updatePlayer(player)
        }
    }
}
