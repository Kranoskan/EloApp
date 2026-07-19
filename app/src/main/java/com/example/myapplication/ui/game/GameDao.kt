package com.example.myapplication.ui.game

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    fun getAllGames(): Flow<List<Game>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game)

    @Update
    suspend fun updateGame(game: Game)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: String): Game?

    @Query("SELECT * FROM attribute_ratings WHERE gameId = :gameId")
    suspend fun getAttributesForGame(gameId: String): List<AttributeRating>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttributeRating(rating: AttributeRating)

    @Delete
    suspend fun deleteGame(game: Game)

    @Query("DELETE FROM attribute_ratings WHERE gameId = :gameId")
    suspend fun deleteAttributesForGame(gameId: String)
}