package com.example.myapplication.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.game.*
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
            matchDao.insertMatch(match)
            teams.forEach { matchDao.insertMatchTeam(it) }
            players.forEach { matchDao.insertMatchPlayer(it) }
        }
    }

    suspend fun getMatchWithDetails(matchId: String): MatchWithDetails {
        return matchDao.getMatchWithDetails(matchId)
    }
}
