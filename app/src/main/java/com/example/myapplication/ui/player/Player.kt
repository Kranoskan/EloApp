package com.example.myapplication.ui.player

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "players")
data class Player(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val imageUri: String? = null
) : Parcelable

data class PlayerWithStrength(
    val player: Player,
    val averageStrength: Double
)
