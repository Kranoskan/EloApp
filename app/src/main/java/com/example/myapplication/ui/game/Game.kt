package com.example.myapplication.ui.game

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "games")
data class Game(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val imageUri: String? = null,
    val maxPlayers: Int? = null,
    val lastTurn: Int? = null,
    val teams: List<String>? = null,
    val expansions: List<String>? = null,
    val specialRules: List<String>? = null,
) : Parcelable

// Convertidor para que Room entienda las listas de texto
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.split(";;;")?.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(";;;")
    }
}
