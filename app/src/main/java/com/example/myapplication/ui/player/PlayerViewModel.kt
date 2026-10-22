package com.example.myapplication.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.game.AppDatabase
import com.example.myapplication.ui.game.Game
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PlayerGameStats(
    val gameId: String,
    val gameName: String,
    val gameImageUri: String? = null,
    val strength: Int = 1200,
    val winProbability: Double = 0.5,
    val matchesPlayed: Int
)

data class PlayerGameDetailData(
    val player: Player,
    val game: Game,
    val matchesPlayed: Int,
    val turnCounts: List<Pair<String, Int>>,
    val teamCounts: List<Pair<String, Int>>,
    val ruleCounts: List<Pair<String, Int>>,
    val expansionCounts: List<Pair<String, Int>>
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playerDao = AppDatabase.getDatabase(application).playerDao()
    private val matchDao = AppDatabase.getDatabase(application).matchDao()
    private val gameDao = AppDatabase.getDatabase(application).gameDao()

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
                    gameId = gameId,
                    gameName = matchDetailsList.first().game.name,
                    gameImageUri = matchDetailsList.first().game.imageUri,
                    matchesPlayed = totalMatches,
                    strength = rating?.strength?.toInt() ?: 1200,
                    winProbability = winProb
                )
            }
        }.asLiveData()
    }

    fun getPlayerGameDetail(playerId: String, gameId: String): LiveData<PlayerGameDetailData?> {
        return combine(
            playerDao.getAllPlayers(),
            gameDao.getAllGames(),
            matchDao.getMatchesWithDetailsForPlayer(playerId)
        ) { players, games, matches ->
            val player = players.find { it.id == playerId } ?: return@combine null
            val game = games.find { it.id == gameId } ?: return@combine null

            val playerMatches = matches.filter { it.match.gameId == gameId }

            val turnMap = mutableMapOf<Int, Int>()
            val teamMap = mutableMapOf<String, Int>()
            val ruleMap = mutableMapOf<String, Int>()
            val expansionMap = mutableMapOf<String, Int>()

            playerMatches.forEach { details ->
                val matchPlayer = details.players.find { it.playerId == playerId }

                matchPlayer?.turn?.let { turn ->
                    turnMap[turn] = (turnMap[turn] ?: 0) + 1
                }

                if (!matchPlayer?.teamName.isNullOrBlank()) {
                    val tName = matchPlayer!!.teamName!!
                    teamMap[tName] = (teamMap[tName] ?: 0) + 1
                }

                matchPlayer?.playerRules?.forEach { rule ->
                    if (rule.isNotBlank()) {
                        ruleMap[rule] = (ruleMap[rule] ?: 0) + 1
                    }
                }

                details.match.usedExpansions?.forEach { exp ->
                    if (exp.isNotBlank()) {
                        expansionMap[exp] = (expansionMap[exp] ?: 0) + 1
                    }
                }
            }

            val turnCounts = turnMap.entries
                .sortedBy { it.key }
                .map { "Turno ${it.key}" to it.value }

            val teamCounts = teamMap.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }

            val ruleCounts = ruleMap.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }

            val expansionCounts = expansionMap.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }

            PlayerGameDetailData(
                player = player,
                game = game,
                matchesPlayed = playerMatches.size,
                turnCounts = turnCounts,
                teamCounts = teamCounts,
                ruleCounts = ruleCounts,
                expansionCounts = expansionCounts
            )
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
