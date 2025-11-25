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
import android.util.Log
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

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

                // Construir combatientes tomando en cuenta la mejor criatura de cada jugador (si existe)
                // Logs de Firebase
                Log.d("CombatVM", "Loaded attacker snapshot for $attackerId")
                Log.d("CombatVM", "Loaded defender snapshot for $defenderId")

                val attacker = buildCombatantFromSnapshot(attackerId, atkSnap)
                val defender = buildCombatantFromSnapshot(defenderId, defSnap)

                startCombat(attacker, defender)
            } catch (e: Exception) {
                _uiState.value = CombatUIState(errorMessage = "Error cargando combatientes: ${e.message}", isLoading = false)
            }
        }
    }

    private fun buildCombatantFromSnapshot(id: String, snap: DataSnapshot): Combatant {
        val name = snap.child("name").getValue(String::class.java) ?: "Player"
        val clan = snap.child("clan").getValue(String::class.java)
        val level = (snap.child("nivel").getValue(Int::class.java) ?: 1)
        // Read coins
        val resources = (snap.child("coins").getValue(Int::class.java) ?: 0)

        // Parsear criaturas del snapshot
        val criaturas = mutableListOf<Criatura>()
        val criSnaps = snap.child("criaturas")
        for (c in criSnaps.children) {
            val criatura = c.getValue(Criatura::class.java)
            if (criatura != null) criaturas.add(criatura)
        }

        val best = criaturas.maxByOrNull { it.poder }

        val attack = best?.dano ?: (5 + level * 3)
        val maxHealth = best?.salud ?: (100 + level * 10)

        return Combatant(
            id = id,
            name = name,
            clan = clan,
            level = level,
            attack = attack,
            maxHealth = maxHealth,
            resources = resources
        )
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

    /**
     * Inicia un combate entre dos IDs, pero usando una criatura seleccionada para el atacante
     * si se proporciona. La criatura debe pertenecer al inventario del atacante (se busca por id).
     */
    fun startCombatWithSelectedCreature(attackerId: String?, defenderId: String?, selectedCriatura: com.example.warofwonders.ui.model.Criatura?) {
        if (attackerId == null || defenderId == null) return

        viewModelScope.launch {
            try {
                val atkSnap = usersRef.child(attackerId).get().await()
                val defSnap = usersRef.child(defenderId).get().await()

                // Logs de Firebase
                Log.d("CombatVM", "Loaded attacker snapshot for $attackerId (selected creature flow)")
                Log.d("CombatVM", "Loaded defender snapshot for $defenderId (selected creature flow)")

                // Construir defensor usando la función existente
                val defender = buildCombatantFromSnapshot(defenderId, defSnap)

                // Validar que la criatura seleccionada pertenezca al atacante
                if (selectedCriatura != null) {
                    var belongs = false
                    val criSnaps = atkSnap.child("criaturas")
                    for (c in criSnaps.children) {
                        val cri = c.getValue(Criatura::class.java)
                        if (cri != null && cri.id == selectedCriatura.id) {
                            belongs = true
                            break
                        }
                    }
                    if (!belongs) {
                        _uiState.value = CombatUIState(errorMessage = "Selected creature does not belong to attacker", isLoading = false)
                        Log.e("CombatVM", "Selected creature ${selectedCriatura.id} does not belong to attacker $attackerId")
                        return@launch
                    }
                }

                // Construir atacante pero aplicando la criatura seleccionada si existe
                val name = atkSnap.child("name").getValue(String::class.java) ?: "Player"
                val clan = atkSnap.child("clan").getValue(String::class.java)
                val level = (atkSnap.child("nivel").getValue(Int::class.java) ?: 1)
                val resources = (atkSnap.child("coins").getValue(Int::class.java) ?: 0)

                val attack = selectedCriatura?.dano ?: (5 + level * 3)
                val maxHealth = selectedCriatura?.salud ?: (100 + level * 10)

                val attacker = Combatant(
                    id = attackerId,
                    name = name,
                    clan = clan,
                    level = level,
                    attack = attack,
                    maxHealth = maxHealth,
                    resources = resources
                )

                startCombat(attacker, defender)
            } catch (e: Exception) {
                _uiState.value = CombatUIState(errorMessage = "Error iniciando combate: ${e.message}", isLoading = false)
            }
        }
    }

    /**
     * Similar a startCombatWithSelectedCreature pero aplica la criatura seleccionada al defensor.
     * La criatura debe pertenecer al defensor (se valida contra su snapshot).
     */
    fun startCombatWithSelectedCreatureForDefender(attackerId: String?, defenderId: String?, selectedCriatura: com.example.warofwonders.ui.model.Criatura?) {
        if (attackerId == null || defenderId == null) return

        viewModelScope.launch {
            try {
                val atkSnap = usersRef.child(attackerId).get().await()
                val defSnap = usersRef.child(defenderId).get().await()

                Log.d("CombatVM", "Loaded attacker snapshot for $attackerId (defender-selected flow)")
                Log.d("CombatVM", "Loaded defender snapshot for $defenderId (defender-selected flow)")

                // Validar que la criatura seleccionada pertenezca al defensor
                if (selectedCriatura != null) {
                    var belongs = false
                    val criSnaps = defSnap.child("criaturas")
                    for (c in criSnaps.children) {
                        val cri = c.getValue(Criatura::class.java)
                        if (cri != null && cri.id == selectedCriatura.id) {
                            belongs = true
                            break
                        }
                    }
                    if (!belongs) {
                        _uiState.value = CombatUIState(errorMessage = "Selected creature does not belong to defender", isLoading = false)
                        Log.e("CombatVM", "Selected creature ${selectedCriatura.id} does not belong to defender $defenderId")
                        return@launch
                    }
                }

                // Construir atacante usando la función existente
                val attacker = buildCombatantFromSnapshot(attackerId, atkSnap)

                // Construir defensor pero aplicando la criatura seleccionada si existe
                val name = defSnap.child("name").getValue(String::class.java) ?: "Player"
                val clan = defSnap.child("clan").getValue(String::class.java)
                val level = (defSnap.child("nivel").getValue(Int::class.java) ?: 1)
                val resources = (defSnap.child("coins").getValue(Int::class.java) ?: 0)

                val attack = selectedCriatura?.dano ?: (5 + level * 3)
                val maxHealth = selectedCriatura?.salud ?: (100 + level * 10)

                val defender = Combatant(
                    id = defenderId,
                    name = name,
                    clan = clan,
                    level = level,
                    attack = attack,
                    maxHealth = maxHealth,
                    resources = resources
                )

                startCombat(attacker, defender)
            } catch (e: Exception) {
                _uiState.value = CombatUIState(errorMessage = "Error iniciando combate (defensor): ${e.message}", isLoading = false)
            }
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
        // Usar una transacción en el nodo "users" para actualizar ambos usuarios atomicamente
        try {
            Log.d("CombatVM", "Applying resource transfer: ${result.resourcesTransferred} from ${result.loserId} to ${result.winnerId}")
            usersRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    try {
                        val winnerData = currentData.child(result.winnerId)
                        val loserData = currentData.child(result.loserId)

                        val winnerCoins = winnerData.child("coins").getValue(Int::class.java) ?: 0
                        val loserCoins = loserData.child("coins").getValue(Int::class.java) ?: 0

                        val transfer = result.resourcesTransferred.coerceAtMost(loserCoins)

                        winnerData.child("coins").value = winnerCoins + transfer
                        loserData.child("coins").value = loserCoins - transfer

                        return Transaction.success(currentData)
                    } catch (e: Exception) {
                        Log.e("CombatVM", "Transaction error: ${e.message}")
                        return Transaction.abort()
                    }
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null) {
                        Log.e("CombatVM", "Transaction failed: ${error.message}")
                    } else if (committed) {
                        Log.d("CombatVM", "Transaction committed: transferred ${result.resourcesTransferred}")
                    } else {
                        Log.w("CombatVM", "Transaction not committed for unknown reason")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("CombatVM", "applyResourceTransfer exception: ${e.message}")
        }
    }
}