package com.example.myapplication.ui.player

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    @Update
    suspend fun updatePlayer(player: Player)

    @Query("SELECT * FROM player_ratings WHERE playerId = :playerId AND gameId = :gameId")
    suspend fun getRating(playerId: String, gameId: String): PlayerRating?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: PlayerRating)

    @Query("SELECT * FROM player_ratings WHERE playerId = :playerId")
    fun getRatingsForPlayer(playerId: String): Flow<List<PlayerRating>>
}
