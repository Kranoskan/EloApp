package com.example.myapplication.ui.game

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "attribute_ratings")
data class AttributeRating(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val type: String, // "TEAM", "RULE", "TURN"
    val name: String,
    val strength: Double = 0.0,
    val matchesPlayed: Int = 0
)
