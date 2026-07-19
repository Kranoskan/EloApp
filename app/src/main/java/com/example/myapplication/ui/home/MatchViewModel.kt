package com.example.myapplication.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.game.*
import com.example.myapplication.ui.player.PlayerRating
import kotlinx.coroutines.launch

class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val matchDao = AppDatabase.getDatabase(application).matchDao()
    private val gameDao = AppDatabase.getDatabase(application).gameDao()
    private val playerDao = AppDatabase.getDatabase(application).playerDao()

    val matches = matchDao.getAllMatchesWithGame().asLiveData()
    val games = gameDao.getAllGames().asLiveData()
    val players = playerDao.getAllPlayers().asLiveData()

    fun addMatch(match: Match, teams: List<MatchTeam>, players: List<MatchPlayer>) {
        viewModelScope.launch {
            // 1. Insertar la partida y sus detalles
            matchDao.insertMatch(match)
            teams.forEach { matchDao.insertMatchTeam(it) }
            players.forEach { matchDao.insertMatchPlayer(it) }

            // 2. Calcular y actualizar Elo
            val game = gameDao.getGameById(match.gameId)
            if (game != null) {
                val playerIds = players.map { it.playerId }.distinct()
                val currentRatings = playerIds.associateWith { playerId ->
                    playerDao.getRating(playerId, game.id) ?: PlayerRating(playerId = playerId, gameId = game.id)
                }
                
                // Obtener ratings de atributos
                val attributeRatingsList = gameDao.getAttributesForGame(game.id)
                val attributeRatingsMap = attributeRatingsList.associateBy { "${it.type}_${it.name}" }

                val (updatedRatings, updatedAttributes) = EloCalculator.calculateEloChanges(
                    game, teams, players, currentRatings, attributeRatingsMap
                )

                updatedRatings.forEach {
                    playerDao.insertRating(it)
                }
                
                updatedAttributes.forEach {
                    gameDao.insertAttributeRating(it)
                }
            }
        }
    }

    suspend fun getMatchWithDetails(matchId: String): MatchWithDetails {
        return matchDao.getMatchWithDetails(matchId)
    }

    fun deleteMatch(match: Match) {
        viewModelScope.launch {
            matchDao.deleteTeamsForMatch(match.id)
            matchDao.deletePlayersForMatch(match.id)
            matchDao.deleteMatch(match)
        }
    }
}
