package com.example.myapplication.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.game.AppDatabase
import kotlinx.coroutines.flow.combine
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

    val playersWithStrength: LiveData<List<PlayerWithStrength>> = combine(
        playerDao.getAllPlayers(),
        playerDao.getAllRatings()
    ) { players, ratings ->
        players.map { player ->
            val playerRatings = ratings.filter { it.playerId == player.id }
            val avg = if (playerRatings.isEmpty()) 1200.0 else playerRatings.map { it.strength }.average()
            PlayerWithStrength(player, avg)
        }
    }.asLiveData()

    fun getPlayerStats(playerId: String): LiveData<List<PlayerGameStats>> {
        return combine(
            matchDao.getMatchesWithDetailsForPlayer(playerId),
            playerDao.getRatingsForPlayer(playerId)
        ) { matches, ratings ->
            matches.groupBy { it.game.id }.map { (gameId, matchDetailsList) ->
                val rating = ratings.find { it.gameId == gameId }
                
                val wins = matchDetailsList.count { detail ->
                    if (detail.match.isTeamGame) {
                        val playerInMatch = detail.players.find { it.playerId == playerId }
                        val playerTeam = detail.teams.find { it.teamName == playerInMatch?.teamName }
                        val maxTeamScore = detail.teams.maxOfOrNull { it.score }
                        playerTeam != null && maxTeamScore != null && playerTeam.score == maxTeamScore
                    } else {
                        val playerScore = detail.players.find { it.playerId == playerId }?.score
                        val maxPlayerScore = detail.players.maxOfOrNull { it.score ?: Int.MIN_VALUE }
                        playerScore != null && maxPlayerScore != null && playerScore == maxPlayerScore
                    }
                }
                
                val totalMatches = matchDetailsList.size
                val winProb = if (totalMatches > 0) wins.toDouble() / totalMatches else 0.0

                PlayerGameStats(
                    gameName = matchDetailsList.first().game.name,
                    matchesPlayed = totalMatches,
                    strength = rating?.strength?.toInt() ?: 1200,
                    winProbability = winProb
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

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            playerDao.deleteRatingsForPlayer(player.id)
            playerDao.deletePlayer(player)
        }
    }
}
