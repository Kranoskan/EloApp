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
    @Query("SELECT * FROM matches ORDER BY date ASC")
    suspend fun getAllMatchesWithDetailsAsc(): List<MatchWithDetails>

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
    fun getMatchesWithDetailsForPlayer(playerId: String): Flow<List<MatchWithDetails>>

    @Delete
    suspend fun deleteMatch(match: Match)

    @Query("DELETE FROM match_teams WHERE matchId = :matchId")
    suspend fun deleteTeamsForMatch(matchId: String)

    @Query("DELETE FROM match_players WHERE matchId = :matchId")
    suspend fun deletePlayersForMatch(matchId: String)

    @Query("DELETE FROM matches WHERE gameId = :gameId")
    suspend fun deleteMatchesForGame(gameId: String)

    @Query("DELETE FROM match_teams WHERE matchId IN (SELECT id FROM matches WHERE gameId = :gameId)")
    suspend fun deleteTeamsForGame(gameId: String)

    @Query("DELETE FROM match_players WHERE matchId IN (SELECT id FROM matches WHERE gameId = :gameId)")
    suspend fun deletePlayersForGame(gameId: String)
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
        parentColumn = "gameId",
        entityColumn = "id"
    )
    val game: Game,
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
