package com.example.warofwonders.data.service

import com.example.warofwonders.data.model.CombatResult
import com.example.warofwonders.data.model.Combatant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Una simulacion de combate pequeño.
 * El metodo retorna un CombatResult con los recursos transferidos del perdedor al ganador.
 */
class CombatService {

    suspend fun fight(attacker: Combatant, defender: Combatant): CombatResult =
        withContext(Dispatchers.Default) {
            var atkHp = attacker.maxHealth
            var defHp = defender.maxHealth
            var rounds = 0
            val rand = Random(System.currentTimeMillis())

            while (atkHp > 0 && defHp > 0) {
                rounds++
                // El atacante golpea
                val atkDamage = attacker.attack + rand.nextInt(0, attacker.level + 3)
                defHp -= atkDamage
                if (defHp <= 0) break

                // El defensor contraataca
                val defDamage = defender.attack + rand.nextInt(0, defender.level + 3)
                atkHp -= defDamage
            }

            val winnerIsAttacker = atkHp > 0
            val winner = if (winnerIsAttacker) attacker else defender
            val loser = if (winnerIsAttacker) defender else attacker

            // Se transifere el 10% de los recursos del perdedor (redondeados) por defecto
            val transferred = ((loser.resources * 0.10).roundToInt()).coerceAtLeast(0)

            CombatResult(
                winnerId = winner.id,
                loserId = loser.id,
                winnerClan = winner.clan,
                loserClan = loser.clan,
                resourcesTransferred = transferred,
                rounds = rounds
            )
        }
}
