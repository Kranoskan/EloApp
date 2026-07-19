package com.example.myapplication.ui.player

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "player_ratings")
data class PlayerRating(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val playerId: String,
    val gameId: String,
    val strength: Double = 1200.0,
    val matchesPlayed: Int = 0
)
