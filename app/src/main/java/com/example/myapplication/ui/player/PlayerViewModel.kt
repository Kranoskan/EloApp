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
    val gameImageUri: String? = null,
    val strength: Int = 1200,
    val winProbability: Double = 0.5,
    val matchesPlayed: Int
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playerDao = AppDatabase.getDatabase(application).playerDao()
    private val matchDao = AppDatabase.getDatabase(application).matchDao()

    val players = playerDao.getAllPlayers().asLiveData()
    val allRatings = playerDao.getAllRatings().asLiveData()

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
                
                val winPoints = matchDetailsList.sumOf { detail ->
                    if (detail.match.isTeamGame) {
                        val playerInMatch = detail.players.find { it.playerId == playerId }
                        val playerTeam = detail.teams.find { it.teamName == playerInMatch?.teamName }
                        val teamScores = detail.teams.map { it.score }
                        val maxTeamScore = teamScores.maxOrNull()
                        val minTeamScore = teamScores.minOrNull()
                        
                        if (playerTeam != null && maxTeamScore != null && minTeamScore != null) {
                            if (maxTeamScore == minTeamScore) 0.5 
                            else if (playerTeam.score == maxTeamScore) 1.0 
                            else 0.0
                        } else 0.0
                    } else {
                        val playerScore = detail.players.find { it.playerId == playerId }?.score
                        val playerScores = detail.players.mapNotNull { it.score }
                        val maxPlayerScore = playerScores.maxOrNull()
                        val minPlayerScore = playerScores.minOrNull()
                        
                        if (playerScore != null && maxPlayerScore != null && minPlayerScore != null) {
                            if (maxPlayerScore == minPlayerScore) 0.5 
                            else if (playerScore == maxPlayerScore) 1.0 
                            else 0.0
                        } else 0.0
                    }
                }
                
                val totalMatches = matchDetailsList.size
                val winProb = if (totalMatches > 0) winPoints / totalMatches else 0.0

                PlayerGameStats(
                    gameName = matchDetailsList.first().game.name,
                    gameImageUri = matchDetailsList.first().game.imageUri,
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
