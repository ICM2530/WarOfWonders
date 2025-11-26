package com.example.warofwonders

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import com.example.warofwonders.data.model.Combatant
import com.example.warofwonders.data.service.CombatService

class CombatServiceTest {

    @Test
    fun testCombat_HighLevelAlwaysWins() = runBlocking {
        val service = CombatService()
        
        val weak = Combatant(
            id = "player1",
            name = "Weak Player",
            level = 1,
            attack = 5,
            maxHealth = 50,
            resources = 100
        )
        
        val strong = Combatant(
            id = "player2",
            name = "Strong Player",
            level = 5,
            attack = 15,
            maxHealth = 150,
            resources = 200
        )
        
        val result = service.fight(weak, strong)
        
        assertEquals(strong.id, result.winnerId)
        assertEquals(weak.id, result.loserId)
        assertTrue(result.resourcesTransferred >= 0)
        assertTrue(result.rounds > 0)
    }

    @Test
    fun testCombat_ResourceTransfer() = runBlocking {
        val service = CombatService()
        
        val attacker = Combatant(
            id = "att1",
            name = "Attacker",
            level = 3,
            attack = 10,
            maxHealth = 100,
            resources = 500
        )
        
        val defender = Combatant(
            id = "def1",
            name = "Defender",
            level = 2,
            attack = 8,
            maxHealth = 80,
            resources = 300
        )
        
        val result = service.fight(attacker, defender)
        
        // el perdedor deberia transferer mas o menos el 10% de los recursos
        val expectedTransfer = (defender.resources * 0.10).toInt()
        assertEquals(expectedTransfer, result.resourcesTransferred)
    }

    @Test
    fun testCombat_BothCanWin() = runBlocking {
        val service = CombatService()
        
        val player1 = Combatant(
            id = "p1",
            name = "Player 1",
            level = 2,
            attack = 8,
            maxHealth = 80,
            resources = 100
        )
        
        val player2 = Combatant(
            id = "p2",
            name = "Player 2",
            level = 2,
            attack = 8,
            maxHealth = 80,
            resources = 100
        )
        
        // correr multiples veces para ver diferentes resultados
        var p1Wins = 0
        var p2Wins = 0
        repeat(10) {
            val result = service.fight(player1, player2)
            if (result.winnerId == player1.id) p1Wins++
            else p2Wins++
        }
        
        // ambos deberian ganar al menos una vez
        assertTrue(p1Wins > 0)
        assertTrue(p2Wins > 0)
    }
}
