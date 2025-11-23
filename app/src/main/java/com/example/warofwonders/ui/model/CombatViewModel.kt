package com.example.warofwonders.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warofwonders.data.model.CombatResult
import com.example.warofwonders.data.model.Combatant
import com.example.warofwonders.data.service.CombatService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Firebase
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

/**
 * El estado de la interfaz de combate.
 */
data class CombatUIState(
    val attacker: Combatant? = null,
    val defender: Combatant? = null,
    val result: CombatResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * El ViewModel para la pantalla de combate.
 * Mantiene el estado de combate y le permite a la interfaz iniciar/reiniciar combates.
 */
class CombatViewModel : ViewModel() {
    private val combatService = CombatService()
    private val realtime = Firebase.database
    private val usersRef: DatabaseReference = realtime.getReference("users")

    private val _uiState = MutableStateFlow(CombatUIState())
    val uiState: StateFlow<CombatUIState> = _uiState.asStateFlow()

    fun startCombat(attacker: Combatant, defender: Combatant) {
        _uiState.value = CombatUIState(attacker = attacker, defender = defender, isLoading = true)

        viewModelScope.launch {
            try {
                val result = combatService.fight(attacker, defender)
                // aplicar la transferencia de recursos en realtime DB
                applyResourceTransfer(result)

                _uiState.value = CombatUIState(attacker = attacker, defender = defender, result = result, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = CombatUIState(errorMessage = "Combat error: ${e.message}", isLoading = false)
            }
        }
    }

    fun startCombatByIds(attackerId: String?, defenderId: String?) {
        if (attackerId == null || defenderId == null) return

        viewModelScope.launch {
            try {
                val atkSnap = usersRef.child(attackerId).get().await()
                val defSnap = usersRef.child(defenderId).get().await()

                val attacker = Combatant(
                    id = attackerId,
                    name = atkSnap.child("name").getValue(String::class.java) ?: "Player",
                    clan = atkSnap.child("clan").getValue(String::class.java),
                    level = (atkSnap.child("nivel").getValue(Int::class.java) ?: 1),
                    attack = 5 + (atkSnap.child("nivel").getValue(Int::class.java) ?: 1) * 3,
                    maxHealth = 100 + (atkSnap.child("nivel").getValue(Int::class.java) ?: 1) * 10,
                    resources = (atkSnap.child("monedas").getValue(Int::class.java) ?: 0)
                )

                val defender = Combatant(
                    id = defenderId,
                    name = defSnap.child("name").getValue(String::class.java) ?: "Player",
                    clan = defSnap.child("clan").getValue(String::class.java),
                    level = (defSnap.child("nivel").getValue(Int::class.java) ?: 1),
                    attack = 5 + (defSnap.child("nivel").getValue(Int::class.java) ?: 1) * 3,
                    maxHealth = 100 + (defSnap.child("nivel").getValue(Int::class.java) ?: 1) * 10,
                    resources = (defSnap.child("monedas").getValue(Int::class.java) ?: 0)
                )

                startCombat(attacker, defender)
            } catch (e: Exception) {
                _uiState.value = CombatUIState(errorMessage = "Error loading combatants: ${e.message}", isLoading = false)
            }
        }
    }

    fun resetCombat() { _uiState.value = CombatUIState() }
    
    fun getWinner(): Combatant? {
        val result = _uiState.value.result ?: return null
        return if (result.winnerId == _uiState.value.attacker?.id) {
            _uiState.value.attacker
        } else {
            _uiState.value.defender
        }
    }
    
    fun getLoser(): Combatant? {
        val result = _uiState.value.result ?: return null
        return if (result.loserId == _uiState.value.attacker?.id) {
            _uiState.value.attacker
        } else {
            _uiState.value.defender
        }
    }

    private fun applyResourceTransfer(result: CombatResult) {
        viewModelScope.launch {
            try {
                val winnerRef = usersRef.child(result.winnerId)
                val loserRef = usersRef.child(result.loserId)

                val winnerSnap = winnerRef.get().await()
                val loserSnap = loserRef.get().await()
                val winnerCoins = (winnerSnap.child("monedas").getValue(Int::class.java) ?: 0)
                val loserCoins = (loserSnap.child("monedas").getValue(Int::class.java) ?: 0)

                val transfer = result.resourcesTransferred.coerceAtMost(loserCoins)
                winnerRef.child("monedas").setValue(winnerCoins + transfer)
                loserRef.child("monedas").setValue(loserCoins - transfer)
            } catch (_: Exception) {
                // para ignorar errores menores
            }
        }
    }
}
