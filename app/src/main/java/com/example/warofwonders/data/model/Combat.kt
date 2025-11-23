package com.example.warofwonders.data.model

data class Combatant(
    val id: String,
    val name: String,
    val clan: String? = null,
    val level: Int = 1,
    val attack: Int = 10,
    val maxHealth: Int = 100,
    val resources: Int = 0 // monedas o las unidades de recursos que vayamos a usar para que el perdedor pierda
)

data class CombatResult(
    val winnerId: String,
    val loserId: String,
    val winnerClan: String?,
    val loserClan: String?,
    val resourcesTransferred: Int,
    val rounds: Int
)

enum class CombatOutcome { WIN, LOSE }
