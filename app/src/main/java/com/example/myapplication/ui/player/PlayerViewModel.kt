package com.example.myapplication.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.game.AppDatabase
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playerDao = AppDatabase.getDatabase(application).playerDao()

    val players = playerDao.getAllPlayers().asLiveData()

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
