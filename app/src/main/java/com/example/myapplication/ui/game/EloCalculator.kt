package com.example.myapplication.ui.game

import com.example.myapplication.ui.player.PlayerRating
import kotlin.math.pow
import kotlin.math.sqrt

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

    /**
     * Paso 2: Fuerza Base del Equipo usando media cuadrática
     */
    private fun calculateBaseTeamStrength(playerStrengths: List<Double>): Double {
        if (playerStrengths.isEmpty()) return 0.0
        val sumSquares = playerStrengths.sumOf { it * it }
        return sqrt(sumSquares / playerStrengths.size)
    }

    private fun extractStrength(text: String): Double {
        val regex = Regex("""\(([+-]?\d+)\)""")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.toDouble() ?: 0.0
    }

    /**
     * Calcula los cambios de Elo para una partida, incluyendo atributos (Turnos, Equipos, Reglas)
     */
    fun calculateEloChanges(
        game: Game,
        teams: List<MatchTeam>,
        matchPlayers: List<MatchPlayer>,
        playerRatings: Map<String, PlayerRating>,
        attributeRatings: Map<String, AttributeRating>
    ): Pair<List<PlayerRating>, List<AttributeRating>> {
        val updatedPlayerRatings = mutableListOf<PlayerRating>()
        val updatedAttributeRatings = mutableListOf<AttributeRating>()

        // 1. Identificar entidades que participan en el cálculo de Elo (Jugadores y Atributos)
        data class EntityData(
            val id: String,
            val name: String,
            val type: String, // "PLAYER", "TEAM", "RULE", "TURN"
            val score: Double,
            val baseRating: Double,
            val matchesPlayed: Int,
            var deltaPoints: Double = 0.0,
            val involvedPlayerRatings: List<Double> = emptyList()
        )

        val participants = mutableListOf<EntityData>()

        // -- Jugadores/Equipos como unidades de competición --
        // (Mantenemos la lógica de equipos vs equipos o FFA)
        data class TeamData(
            val name: String,
            val players: List<MatchPlayer>,
            val score: Double,
            var virtualStrength: Double = 0.0,
            var deltaPoints: Double = 0.0
        )

        val activeTeams = if (game.teams != null && teams.isNotEmpty()) {
            teams.map { mt ->
                val teamPlayers = matchPlayers.filter { it.teamName == mt.teamName }
                val functionalStrengths = teamPlayers.map { mp ->
                    val rating = playerRatings[mp.playerId]?.strength ?: 1200.0
                    val rulesStr = mp.playerRules?.sumOf { extractStrength(it) } ?: 0.0
                    val attrRulesStr = mp.playerRules?.sumOf { ruleName -> 
                        attributeRatings["RULE_$ruleName"]?.strength ?: 0.0 
                    } ?: 0.0
                    rating + rulesStr + attrRulesStr
                }
                val baseStr = calculateBaseTeamStrength(functionalStrengths)
                val factionBonus = extractStrength(mt.teamName)
                val factionAttrStr = attributeRatings["TEAM_${mt.teamName}"]?.strength ?: 0.0
                TeamData(mt.teamName, teamPlayers, mt.score.toDouble(), virtualStrength = baseStr + factionBonus + factionAttrStr)
            }
        } else {
            matchPlayers.map { mp ->
                val rating = playerRatings[mp.playerId]?.strength ?: 1200.0
                val rulesStr = mp.playerRules?.sumOf { extractStrength(it) } ?: 0.0
                val attrRulesStr = mp.playerRules?.sumOf { ruleName -> 
                    attributeRatings["RULE_$ruleName"]?.strength ?: 0.0 
                } ?: 0.0
                TeamData(mp.playerId, listOf(mp), mp.score?.toDouble() ?: 0.0, virtualStrength = rating + rulesStr + attrRulesStr)
            }
        }

        // -- Atributos como "Jugadores virtuales" --
        // Turnos
        val turnsUsed = matchPlayers.mapNotNull { it.turn }.distinct()
        turnsUsed.forEach { turnNum ->
            val attrName = "Turno $turnNum"
            val key = "TURN_$attrName"
            val playersInTurn = matchPlayers.filter { it.turn == turnNum }
            val scores = if (game.teams != null) {
                playersInTurn.mapNotNull { mp -> teams.find { it.teamName == mp.teamName }?.score?.toDouble() }
            } else {
                playersInTurn.mapNotNull { it.score?.toDouble() }
            }
            val avgScore = if (scores.isEmpty()) 0.0 else scores.average()
            val rating = attributeRatings[key] ?: AttributeRating(gameId = game.id, type = "TURN", name = attrName)
            
            val playerStrengths = playersInTurn.map { playerRatings[it.playerId]?.strength ?: 1200.0 }
            val virtualStr = rating.strength + (if (playerStrengths.isEmpty()) 1200.0 else playerStrengths.average())
            
            participants.add(EntityData(key, attrName, "TURN", avgScore, rating.strength, rating.matchesPlayed, involvedPlayerRatings = playerStrengths))
        }

        // Reglas Especiales
        val rulesUsed = matchPlayers.flatMap { it.playerRules ?: emptyList() }.distinct()
        rulesUsed.forEach { ruleName ->
            val key = "RULE_$ruleName"
            val playersWithRule = matchPlayers.filter { it.playerRules?.contains(ruleName) == true }
            val scores = if (game.teams != null) {
                playersWithRule.mapNotNull { mp -> teams.find { it.teamName == mp.teamName }?.score?.toDouble() }
            } else {
                playersWithRule.mapNotNull { it.score?.toDouble() }
            }
            val avgScore = if (scores.isEmpty()) 0.0 else scores.average()
            val rating = attributeRatings[key] ?: AttributeRating(gameId = game.id, type = "RULE", name = ruleName)
            
            val playerStrengths = playersWithRule.map { playerRatings[it.playerId]?.strength ?: 1200.0 }
            val virtualStr = rating.strength + (if (playerStrengths.isEmpty()) 1200.0 else playerStrengths.average())
            
            participants.add(EntityData(key, ruleName, "RULE", avgScore, rating.strength, rating.matchesPlayed, involvedPlayerRatings = playerStrengths))
        }

        // Facciones / Equipos
        if (game.teams != null) {
            teams.forEach { mt ->
                val key = "TEAM_${mt.teamName}"
                val rating = attributeRatings[key] ?: AttributeRating(gameId = game.id, type = "TEAM", name = mt.teamName)
                val teamPlayers = matchPlayers.filter { it.teamName == mt.teamName }
                val playerStrengths = teamPlayers.map { playerRatings[it.playerId]?.strength ?: 1200.0 }
                
                participants.add(EntityData(key, mt.teamName, "TEAM", mt.score.toDouble(), rating.strength, rating.matchesPlayed, involvedPlayerRatings = playerStrengths))
            }
        }

        // 2. Calcular Deltas para Equipos de Jugadores (Lógica existente)
        val maxScore = (activeTeams.map { it.score } + participants.map { it.score }).maxOf { it }.coerceAtLeast(1.0)
        
        for (i in activeTeams.indices) {
            val teamA = activeTeams[i]
            var totalDelta = 0.0
            var rivals = 0
            for (j in activeTeams.indices) {
                if (i == j) continue
                totalDelta += (teamA.score / maxScore) - calculateExpectedOutcome(teamA.virtualStrength, activeTeams[j].virtualStrength)
                rivals++
            }
            teamA.deltaPoints = totalDelta / rivals.coerceAtLeast(1)
        }

        // 3. Calcular Deltas para Atributos (Tratados como jugadores)
        for (pA in participants) {
            var totalDelta = 0.0
            var rivals = 0
            val virtualStrA = pA.baseRating + (if (pA.involvedPlayerRatings.isEmpty()) 1200.0 else pA.involvedPlayerRatings.average())
            
            // Compiten contra los equipos reales
            for (teamB in activeTeams) {
                totalDelta += (pA.score / maxScore) - calculateExpectedOutcome(virtualStrA, teamB.virtualStrength)
                rivals++
            }
            pA.deltaPoints = totalDelta / rivals.coerceAtLeast(1)
        }

        // 4. Aplicar cambios a Jugadores
        for (team in activeTeams) {
            val teamRealStrengths = team.players.map { playerRatings[it.playerId]?.strength ?: 1200.0 }
            val meanRealStrength = if (teamRealStrengths.isEmpty()) 1200.0 else teamRealStrengths.average()
            for (mp in team.players) {
                val currentRating = playerRatings[mp.playerId] ?: PlayerRating(playerId = mp.playerId, gameId = game.id)
                val k = getKFactor(currentRating.matchesPlayed)
                val teamDelta = team.deltaPoints * k
                val playerDelta = if (teamDelta >= 0) teamDelta * (meanRealStrength / currentRating.strength) else teamDelta * (currentRating.strength / meanRealStrength)
                updatedPlayerRatings.add(currentRating.copy(strength = currentRating.strength + playerDelta, matchesPlayed = currentRating.matchesPlayed + 1))
            }
        }

        // 5. Aplicar cambios a Atributos
        for (p in participants) {
            val currentRating = attributeRatings[p.id] ?: AttributeRating(gameId = game.id, type = p.type, name = p.name)
            val k = getKFactor(currentRating.matchesPlayed)
            val delta = p.deltaPoints * k
            updatedAttributeRatings.add(currentRating.copy(strength = currentRating.strength + delta, matchesPlayed = currentRating.matchesPlayed + 1))
        }

        return Pair(updatedPlayerRatings, updatedAttributeRatings)
    }
}
