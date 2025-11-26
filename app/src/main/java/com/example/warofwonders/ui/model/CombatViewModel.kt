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
import kotlin.math.roundToInt
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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
    ,
    // HP actuales durante el combate (se actualizan en tiempo real)
    val attackerHp: Int? = null,
    val defenderHp: Int? = null
)

/**
 * El ViewModel para la pantalla de combate.
 * Mantiene el estado de combate y le permite a la interfaz iniciar/reiniciar combates.
 */
class CombatViewModel : ViewModel() {
    private val combatService = CombatService()
    private val realtime = Firebase.database
    private val usersRef: DatabaseReference = realtime.getReference("users")
    private val encountersRef: DatabaseReference = realtime.getReference("encounters")
    private var encounterListener: ValueEventListener? = null
    private var currentEncounterRef: DatabaseReference? = null

    private val _uiState = MutableStateFlow(CombatUIState())
    val uiState: StateFlow<CombatUIState> = _uiState.asStateFlow()

    fun startCombat(attacker: Combatant, defender: Combatant, encounterKey: String? = null) {
        // Inicializar estado con HP completos
        _uiState.value = CombatUIState(attacker = attacker, defender = defender, isLoading = true, attackerHp = attacker.maxHealth, defenderHp = defender.maxHealth)

        viewModelScope.launch {
            try {
                // Simular rounds con actualizaciones en tiempo real y pequeñas pausas para que la UI las muestre
                var atkHp = attacker.maxHealth
                var defHp = defender.maxHealth
                var rounds = 0
                val rand = kotlin.random.Random(System.currentTimeMillis())

                while (atkHp > 0 && defHp > 0) {
                    rounds++
                    // El atacante golpea
                    val atkDamage = attacker.attack + rand.nextInt(0, attacker.level + 3)
                    defHp -= atkDamage
                    if (defHp < 0) defHp = 0

                    // Actualizar estado para que la UI muestre el daño
                    _uiState.value = _uiState.value.copy(defenderHp = defHp, attacker = attacker, defender = defender)

                    // Publicar progreso en realtime si estamos en un encounter
                    try {
                        if (!encounterKey.isNullOrBlank()) {
                            val progressRef = encountersRef.child(encounterKey).child("progress")
                            val upd: MutableMap<String, Any> = mutableMapOf()
                            upd["attackerHp"] = _uiState.value.attackerHp ?: attacker.maxHealth
                            upd["defenderHp"] = defHp
                            upd["round"] = rounds
                            upd["lastUpdated"] = ServerValue.TIMESTAMP
                            progressRef.updateChildren(upd as Map<String, Any>)
                        }
                    } catch (e: Exception) {
                        Log.w("CombatVM", "Failed to publish progress: ${e.message}")
                    }

                    // Pequeña pausa para visibilidad
                    kotlinx.coroutines.delay(600)

                    if (defHp <= 0) break

                    // El defensor contraataca
                    val defDamage = defender.attack + rand.nextInt(0, defender.level + 3)
                    atkHp -= defDamage
                    if (atkHp < 0) atkHp = 0

                    // Actualizar estado
                    _uiState.value = _uiState.value.copy(attackerHp = atkHp, attacker = attacker, defender = defender)

                    // Publicar progreso en realtime si estamos en un encounter (después del contraataque)
                    try {
                        if (!encounterKey.isNullOrBlank()) {
                            val progressRef = encountersRef.child(encounterKey).child("progress")
                            val upd: MutableMap<String, Any> = mutableMapOf()
                            upd["attackerHp"] = atkHp
                            upd["defenderHp"] = _uiState.value.defenderHp ?: defender.maxHealth
                            upd["round"] = rounds
                            upd["lastUpdated"] = ServerValue.TIMESTAMP
                            progressRef.updateChildren(upd as Map<String, Any>)
                        }
                    } catch (e: Exception) {
                        Log.w("CombatVM", "Failed to publish progress: ${e.message}")
                    }

                    // Pausa entre rondas
                    kotlinx.coroutines.delay(600)
                }

                val winnerIsAttacker = atkHp > 0
                val winner = if (winnerIsAttacker) attacker else defender
                val loser = if (winnerIsAttacker) defender else attacker

                // Se transifere el 10% de los recursos del perdedor (redondeados) por defecto
                val transferred = ((loser.resources * 0.10).roundToInt()).coerceAtLeast(0)

                val result = CombatResult(
                    winnerId = winner.id,
                    loserId = loser.id,
                    winnerClan = winner.clan,
                    loserClan = loser.clan,
                    resourcesTransferred = transferred,
                    rounds = rounds
                )

                // aplicar la transferencia de recursos en realtime DB
                applyResourceTransfer(result)

                // Si este combate pertenece a un encounter, publicar resultado y limpiar el nodo después de un breve delay
                try {
                    if (!encounterKey.isNullOrBlank()) {
                        val rref = encountersRef.child(encounterKey).child("result")
                        val mapRes: MutableMap<String, Any> = mutableMapOf()
                        mapRes["winnerId"] = result.winnerId
                        mapRes["loserId"] = result.loserId
                        mapRes["resourcesTransferred"] = result.resourcesTransferred
                        mapRes["rounds"] = result.rounds
                        mapRes["timestamp"] = ServerValue.TIMESTAMP
                        rref.setValue(mapRes)

                        // Programar limpieza tras 5s para permitir que el cliente receptor lea el resultado
                        viewModelScope.launch {
                            try {
                                kotlinx.coroutines.delay(5000)
                                encountersRef.child(encounterKey).removeValue()
                            } catch (e: Exception) {
                                Log.w("CombatVM", "Failed to cleanup encounter node: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("CombatVM", "Failed to write encounter result: ${e.message}")
                }

                // Actualizar estado final (HP y resultado)
                _uiState.value = CombatUIState(attacker = attacker, defender = defender, result = result, isLoading = false, attackerHp = atkHp, defenderHp = defHp)
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

                // Detectar si es un combate amistoso con apuesta
                var stakeAmount = 0
                try {
                    val reqRef = realtime.getReference("battleRequestsSent").child(attackerId).child(defenderId)
                    val snap = reqRef.get().await()
                    if (snap.exists()) {
                        val s = snap.child("stake").getValue(Int::class.java) ?: snap.child("stake").getValue(Long::class.java)?.toInt()
                        if (s != null) stakeAmount = s
                    } else {
                        // Revisar si hay una invitacion pendiente
                        val otherRef = realtime.getReference("battleRequests").child(attackerId).child(defenderId)
                        val snap2 = otherRef.get().await()
                        val s2 = snap2.child("stake").getValue(Int::class.java) ?: snap2.child("stake").getValue(Long::class.java)?.toInt()
                        if (s2 != null) stakeAmount = s2
                    }
                } catch (e: Exception) {
                    Log.w("CombatVM", "Could not read stake for friendly battle: ${e.message}")
                }

                // Iniciar combate, y si hay apuesta, aplicarla en la transferencia
                if (stakeAmount > 0) {
                    startCombat(attacker, defender)
                    // Despues del combate, aplicar la transferencia de la apuesta
                    // Para esto, esperar los resultados y luego aplicar la transferencia
                    viewModelScope.launch {
                        var attempts = 0
                        while (_uiState.value.result == null && attempts < 60) {
                            kotlinx.coroutines.delay(200)
                            attempts++
                        }
                        val res = _uiState.value.result
                        if (res != null) {
                            // hacer la transferencia de la apuesta
                            applyStakeTransfer(res.winnerId, res.loserId, stakeAmount)
                            // limpiar las solicitudes de combate amistoso
                            try {
                                val a = attackerId
                                val d = defenderId
                                realtime.getReference("battleRequests").child(d).child(a).removeValue()
                                realtime.getReference("battleRequestsSent").child(a).child(d).removeValue()
                            } catch (_: Exception) {}
                        }
                    }
                } else {
                    startCombat(attacker, defender)
                }
            } catch (e: Exception) {
                _uiState.value = CombatUIState(errorMessage = "Error cargando combatientes: ${e.message}", isLoading = false)
            }
        }
    }

    private fun buildCombatantFromSnapshot(id: String, snap: DataSnapshot): Combatant {
        val name = snap.child("name").getValue(String::class.java) ?: "Player"
        val clan = snap.child("clan").getValue(String::class.java)
        val level = (snap.child("nivel").getValue(Int::class.java) ?: 1)
        // Leer monedas
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

        val combatant = Combatant(
            id = id,
            name = name,
            clan = clan,
            level = level,
            attack = attack,
            maxHealth = maxHealth,
            resources = resources
        )

        // poner imagen de criatura si es posible
        combatant.creatureImage = best?.imagen
        return combatant
    }

    // Cargar previews sin iniciar combate
    fun loadCombatantsPreview(attackerId: String?, defenderId: String?) {
        if (attackerId == null || defenderId == null) return

        viewModelScope.launch {
            try {
                val atkSnap = usersRef.child(attackerId).get().await()
                val defSnap = usersRef.child(defenderId).get().await()

                val attacker = buildCombatantFromSnapshot(attackerId, atkSnap)
                val defender = buildCombatantFromSnapshot(defenderId, defSnap)

                _uiState.value = _uiState.value.copy(attacker = attacker, defender = defender)
            } catch (e: Exception) {
                Log.w("CombatVM", "Failed to load combatant previews: ${e.message}")
            }
        }
    }

    private fun encounterKeyFor(a: String, b: String): String {
        val ids = listOf(a, b).sorted()
        return "encounter_${ids[0]}_${ids[1]}"
    }

    /**
     * Inicia la escucha para el encuentro compartido. Flujo simplificado:
     * - Si el encuentro existe, cargar combatientes e iniciar automáticamente.
     * - Escuchar cambios en progress y result.
     */
    fun startEncounterListener(attackerId: String?, defenderId: String?) {
        if (attackerId == null || defenderId == null) return
        val key = encounterKeyFor(attackerId, defenderId)
        currentEncounterRef = encountersRef.child(key)

        // Evitar re-registrar listeners
        encounterListener?.let { currentEncounterRef?.removeEventListener(it) }

        encounterListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // Si el encuentro no existe, no hacer nada
                    if (!snapshot.exists()) return

                    // Si hay progreso publicado, actualizar UI
                    val progressSnap = snapshot.child("progress")
                    if (progressSnap.exists()) {
                        try {
                            val atkHp = progressSnap.child("attackerHp").getValue(Int::class.java)
                            val defHp = progressSnap.child("defenderHp").getValue(Int::class.java)
                            val round = progressSnap.child("round").getValue(Int::class.java) ?: 0
                            var attackerLocal = _uiState.value.attacker
                            var defenderLocal = _uiState.value.defender
                            // Mantener combatants actuales si existen
                            _uiState.value = _uiState.value.copy(
                                attacker = attackerLocal,
                                defender = defenderLocal,
                                attackerHp = atkHp ?: _uiState.value.attackerHp,
                                defenderHp = defHp ?: _uiState.value.defenderHp,
                                isLoading = true
                            )
                        } catch (e: Exception) {
                            Log.w("CombatVM", "Failed parsing progress snapshot: ${e.message}")
                        }
                    }

                    // Si hay resultado, actualizar UI con el resultado
                    val resultSnap = snapshot.child("result")
                    if (resultSnap.exists()) {
                        try {
                            val winnerId = resultSnap.child("winnerId").getValue(String::class.java)
                            val loserId = resultSnap.child("loserId").getValue(String::class.java)
                            val resourcesTransferred = resultSnap.child("resourcesTransferred").getValue(Int::class.java) ?: 0
                            val rounds = resultSnap.child("rounds").getValue(Int::class.java) ?: 0
                            val res = CombatResult(
                                winnerId = winnerId ?: "",
                                loserId = loserId ?: "",
                                winnerClan = null,
                                loserClan = null,
                                resourcesTransferred = resourcesTransferred,
                                rounds = rounds
                            )
                            _uiState.value = _uiState.value.copy(result = res, isLoading = false)
                        } catch (e: Exception) {
                            Log.w("CombatVM", "Failed parsing result snapshot: ${e.message}")
                        }
                    }

                    // Si no hemos iniciado combate aún y no hay resultado, iniciarlo automáticamente
                    if (_uiState.value.attacker != null && _uiState.value.defender != null && !_uiState.value.isLoading && _uiState.value.result == null) {
                        Log.d("CombatVM", "Auto-starting combat for $attackerId vs $defenderId")
                        startCombatByIds(attackerId, defenderId)
                    }
                } catch (e: Exception) {
                    Log.w("CombatVM", "encounter listener error: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("CombatVM", "encounter listener cancelled: ${error.message}")
            }
        }

        currentEncounterRef?.addValueEventListener(encounterListener as ValueEventListener)
    }

    /**
     * Simplificar: Abandonar encuentro (eliminar el nodo si está vacío).
     */
    fun leaveEncounter(attackerId: String?, defenderId: String?) {
        if (attackerId == null || defenderId == null) return
        val key = encounterKeyFor(attackerId, defenderId)
        val ref = encountersRef.child(key)

        // Simplemente intentar remover el nodo de encuentro
        ref.removeValue().addOnSuccessListener {
            Log.d("CombatVM", "leaveEncounter completed for $key")
        }.addOnFailureListener { ex ->
            Log.w("CombatVM", "leaveEncounter failed: ${ex.message}")
        }
    }

    fun resetCombat() {
        // Intentar limpiar cualquier rastro en encounters para este usuario
        try {
            val attackerId = _uiState.value.attacker?.id
            val defenderId = _uiState.value.defender?.id
            if (!attackerId.isNullOrBlank() && !defenderId.isNullOrBlank()) {
                leaveEncounter(attackerId, defenderId)
            }
        } catch (_: Exception) {}

        _uiState.value = CombatUIState()
    }

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

                // poner imagen de criatura si es posible
                attacker.creatureImage = selectedCriatura?.imagen

                startCombat(attacker, defender)
            } catch (e: Exception) {
                _uiState.value = CombatUIState(errorMessage = "Error iniciando combate: ${e.message}", isLoading = false)
            }
        }
    }

    /**
     * Forzar inicio de combate localmente aun si uno de los IDs es null.
     * - Si ambos IDs están presentes, delega a las funciones existentes.
     * - Si falta el defensor, crea un combatiente temporal y corre el combate localmente.
     */
    fun forceStartCombat(attackerId: String?, defenderId: String?, selectedCriatura: com.example.warofwonders.ui.model.Criatura?) {
        viewModelScope.launch {
            try {
                // Ambos presentes: intentar iniciar normalmente (aplica selección si corresponde)
                if (!attackerId.isNullOrBlank() && !defenderId.isNullOrBlank()) {
                    if (selectedCriatura != null && attackerId == FirebaseAuth.getInstance().currentUser?.uid) {
                        startCombatWithSelectedCreature(attackerId, defenderId, selectedCriatura)
                        return@launch
                    }
                    if (selectedCriatura != null && defenderId == FirebaseAuth.getInstance().currentUser?.uid) {
                        startCombatWithSelectedCreatureForDefender(attackerId, defenderId, selectedCriatura)
                        return@launch
                    }
                    startCombatByIds(attackerId, defenderId)
                    return@launch
                }

                // Si falta el defensor, crear un combatiente temporal
                if (!attackerId.isNullOrBlank() && defenderId.isNullOrBlank()) {
                    val atkSnap = usersRef.child(attackerId).get().await()
                    val attacker = if (selectedCriatura != null) {
                        // Validar pertenece al atacante
                        var belongs = false
                        val criSnaps = atkSnap.child("criaturas")
                        for (c in criSnaps.children) {
                            val cri = c.getValue(Criatura::class.java)
                            if (cri != null && cri.id == selectedCriatura.id) { belongs = true; break }
                        }
                        if (belongs) {
                            val name = atkSnap.child("name").getValue(String::class.java) ?: "Player"
                            val clan = atkSnap.child("clan").getValue(String::class.java)
                            val level = (atkSnap.child("nivel").getValue(Int::class.java) ?: 1)
                            val resources = (atkSnap.child("coins").getValue(Int::class.java) ?: 0)
                            val attack = selectedCriatura.dano ?: (5 + level * 3)
                            val maxHealth = selectedCriatura.salud ?: (100 + level * 10)
                            Combatant(id = attackerId, name = name, clan = clan, level = level, attack = attack, maxHealth = maxHealth, resources = resources).also { it.creatureImage = selectedCriatura.imagen }
                        } else buildCombatantFromSnapshot(attackerId, atkSnap)
                    } else buildCombatantFromSnapshot(attackerId, atkSnap)

                    val dummy = Combatant(id = "opponent_temp", name = "Oponente", clan = null, level = 1, attack = 8, maxHealth = 100, resources = 0)
                    startCombat(attacker, dummy)
                    return@launch
                }

                // Si falta el atacante pero hay defensor
                if (attackerId.isNullOrBlank() && !defenderId.isNullOrBlank()) {
                    val defSnap = usersRef.child(defenderId).get().await()
                    val defender = if (selectedCriatura != null) {
                        var belongs = false
                        val criSnaps = defSnap.child("criaturas")
                        for (c in criSnaps.children) {
                            val cri = c.getValue(Criatura::class.java)
                            if (cri != null && cri.id == selectedCriatura.id) { belongs = true; break }
                        }
                        if (belongs) {
                            val name = defSnap.child("name").getValue(String::class.java) ?: "Player"
                            val clan = defSnap.child("clan").getValue(String::class.java)
                            val level = (defSnap.child("nivel").getValue(Int::class.java) ?: 1)
                            val resources = (defSnap.child("coins").getValue(Int::class.java) ?: 0)
                            val attack = selectedCriatura.dano ?: (5 + level * 3)
                            val maxHealth = selectedCriatura.salud ?: (100 + level * 10)
                            Combatant(id = defenderId, name = name, clan = clan, level = level, attack = attack, maxHealth = maxHealth, resources = resources).also { it.creatureImage = selectedCriatura.imagen }
                        } else buildCombatantFromSnapshot(defenderId, defSnap)
                    } else buildCombatantFromSnapshot(defenderId, defSnap)

                    val dummyAtk = Combatant(id = "opponent_temp", name = "Oponente", clan = null, level = 1, attack = 8, maxHealth = 100, resources = 0)
                    startCombat(dummyAtk, defender)
                    return@launch
                }

            } catch (e: Exception) {
                Log.e("CombatVM", "forceStartCombat error: ${e.message}")
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

                defender.creatureImage = selectedCriatura?.imagen

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

    private fun applyStakeTransfer(winnerId: String, loserId: String, stake: Int) {
        try {
            Log.d("CombatVM", "Applying stake transfer: $stake from $loserId to $winnerId")
            usersRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    try {
                        val winnerData = currentData.child(winnerId)
                        val loserData = currentData.child(loserId)

                        val winnerCoins = winnerData.child("coins").getValue(Int::class.java) ?: 0
                        val loserCoins = loserData.child("coins").getValue(Int::class.java) ?: 0

                        val transfer = stake.coerceAtMost(loserCoins)

                        winnerData.child("coins").value = winnerCoins + transfer
                        loserData.child("coins").value = loserCoins - transfer

                        return Transaction.success(currentData)
                    } catch (e: Exception) {
                        Log.e("CombatVM", "Stake transaction error: ${e.message}")
                        return Transaction.abort()
                    }
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null) {
                        Log.e("CombatVM", "Stake transaction failed: ${error.message}")
                    } else if (committed) {
                        Log.d("CombatVM", "Stake transaction committed: transferred $stake")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("CombatVM", "applyStakeTransfer exception: ${e.message}")
        }
    }
}
