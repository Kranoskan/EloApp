package com.example.myapplication.ui.game

import com.example.myapplication.ui.player.PlayerRating
import kotlin.math.pow

object EloCalculator {

    private fun getKFactor(matchesPlayed: Int): Int {
        return when {
            matchesPlayed <= 5 -> 60
            matchesPlayed <= 15 -> 40
            matchesPlayed <= 50 -> 32
            else -> 20
        }
    }

    private fun calculateExpectedOutcome(ratingA: Double, ratingB: Double): Double {
        return 1.0 / (1.0 + 10.0.pow((ratingB - ratingA) / 400.0))
    }

    fun calculateEloChanges(
        game: Game,
        teams: List<MatchTeam>,
        matchPlayers: List<MatchPlayer>,
        playerRatings: Map<String, PlayerRating>,
        attributeRatings: Map<String, AttributeRating>
    ): Pair<List<PlayerRating>, List<AttributeRating>> {

        val updatedPlayerRatings = mutableListOf<PlayerRating>()
        val updatedAttributeRatings = mutableListOf<AttributeRating>()

        // =====================================================================
        // CASO 1 VS 1 PURO (Sin equipos ni información adicional)
        // =====================================================================
        if (matchPlayers.size == 2 && teams.isEmpty()) {
            val player1 = matchPlayers[0]
            val player2 = matchPlayers[1]

            val rating1 = playerRatings[player1.playerId] ?: PlayerRating(playerId = player1.playerId, gameId = game.id)
            val rating2 = playerRatings[player2.playerId] ?: PlayerRating(playerId = player2.playerId, gameId = game.id)

            val score1 = player1.score?.toDouble() ?: 0.0
            val score2 = player2.score?.toDouble() ?: 0.0

            // Resultado del duelo: 1.0 (Gana P1), 0.0 (Gana P2), 0.5 (Empate)
            val outcome1 = when {
                score1 > score2 -> 1.0
                score1 < score2 -> 0.0
                else -> 0.5
            }
            val outcome2 = 1.0 - outcome1

            val expected1 = calculateExpectedOutcome(rating1.strength, rating2.strength)
            val expected2 = calculateExpectedOutcome(rating2.strength, rating1.strength)

            val k1 = getKFactor(rating1.matchesPlayed)
            val k2 = getKFactor(rating2.matchesPlayed)

            val delta1 = k1 * (outcome1 - expected1)
            val delta2 = k2 * (outcome2 - expected2)

            updatedPlayerRatings.add(
                rating1.copy(
                    strength = rating1.strength + delta1,
                    matchesPlayed = rating1.matchesPlayed + 1
                )
            )
            updatedPlayerRatings.add(
                rating2.copy(
                    strength = rating2.strength + delta2,
                    matchesPlayed = rating2.matchesPlayed + 1
                )
            )

            // Devolvemos directamente sin procesar ningún atributo
            return Pair(updatedPlayerRatings, updatedAttributeRatings)
        }

        // Aquí continuaría la lógica de equipos/atributos para partidas complejas...
        return Pair(updatedPlayerRatings, updatedAttributeRatings)
    }
}