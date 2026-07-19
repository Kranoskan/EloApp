package com.example.myapplication.ui.game

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Transaction
    @Query("SELECT * FROM matches ORDER BY date DESC")
    fun getAllMatchesWithGame(): Flow<List<MatchWithGame>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchTeam(matchTeam: MatchTeam)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchPlayer(matchPlayer: MatchPlayer)

    @Transaction
    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchWithDetails(matchId: String): MatchWithDetails

    @Query("SELECT * FROM match_teams WHERE matchId = :matchId")
    suspend fun getTeamsForMatch(matchId: String): List<MatchTeam>

    @Query("SELECT * FROM match_players WHERE matchId = :matchId")
    suspend fun getPlayersForMatch(matchId: String): List<MatchPlayer>

    @Query("SELECT COUNT(*) FROM matches WHERE gameId = :gameId")
    fun getMatchCountForGame(gameId: String): Flow<Int>

    @Transaction
    @Query("SELECT * FROM matches WHERE id IN (SELECT matchId FROM match_players WHERE playerId = :playerId)")
    fun getMatchesForPlayer(playerId: String): Flow<List<MatchWithGame>>
}

data class MatchWithGame(
    @Embedded val match: Match,
    @Relation(
        parentColumn = "gameId",
        entityColumn = "id"
    )
    val game: Game
)

data class MatchWithDetails(
    @Embedded val match: Match,
    @Relation(
        parentColumn = "id",
        entityColumn = "matchId"
    )
    val teams: List<MatchTeam>,
    @Relation(
        parentColumn = "id",
        entityColumn = "matchId"
    )
    val players: List<MatchPlayer>
)
