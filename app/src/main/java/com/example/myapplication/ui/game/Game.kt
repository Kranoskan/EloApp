package com.example.myapplication.ui.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Game(
    val name: String,
    val imageUri: String? = null,
    val maxPlayers: Int? = null,
    val lastTurn: Int? = null,
    val teams: List<String>? = null,
    val expansions: List<String>? = null,
    val specialRules: List<String>? = null
) : Parcelable