package com.example.myapplication.ui.game

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "matches")
data class Match(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val date: Long = System.currentTimeMillis(),
    val usedExpansions: List<String>? = null,
    val isTeamGame: Boolean
) : Parcelable

@Parcelize
@Entity(tableName = "match_teams")
data class MatchTeam(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val matchId: String,
    val teamName: String,
    val score: Int
) : Parcelable

@Parcelize
@Entity(tableName = "match_players")
data class MatchPlayer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val matchId: String,
    val playerId: String,
    val teamName: String? = null,
    val score: Int? = null,
    val turn: Int? = null,
    val playerRules: List<String>? = null
) : Parcelable
