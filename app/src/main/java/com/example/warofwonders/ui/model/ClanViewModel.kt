package com.example.warofwonders.ui.screens.clan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warofwonders.ui.model.Clan
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.model.Zona
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class ClanViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference.child("clanes")
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")

    private val _clanes = MutableStateFlow<List<Clan>>(emptyList())
    val clanes = _clanes.asStateFlow()

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()


    private val _currentUser = MutableStateFlow<MyUserState?>(null)
    val currentUser = _currentUser.asStateFlow()

    // -----------------------------------------------------------
    // CARGAR CLANES DESDE LA DB
    // Compatible con: miembros: { uid: true }
    // -----------------------------------------------------------
    fun cargarClanes() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Clan>()

                for (clanSnap in snapshot.children) {
                    try {
                        val clan = clanSnap.getValue(Clan::class.java)
                        if (clan != null) lista.add(clan)
                    } catch (e: Exception) {
                        Log.e("ClanVM", "Error parsing clan: ${e.message}")
                    }
                }

                _clanes.value = lista
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ClanVM", "Error al cargar clanes: ${error.message}")
            }
        })
    }

    // -----------------------------------------------------------
    // CREAR CLAN (el creador tiene rol = lider)
    // miembros = { uid: true } según tu DB
    // -----------------------------------------------------------

    fun crearClan(
        nombre: String,
        descripcion: String,
        user: MyUserState
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {

            // 1. obtener la ubicación real del usuario
            val lastLoc = obtenerLastLocation(uid)

            if (lastLoc == null) {
                _mensaje.value = "No se pudo obtener la ubicación del usuario"
                return@launch
            }

            // 2. verificar si ya existe un clan en estas coordenadas
            val listaClanes = _clanes.value

            for (clan in listaClanes) {
                val zonaClan = clan.zona
                if (estaDentroDeZona(lastLoc.latitude, lastLoc.longitude, zonaClan)) {
                    _mensaje.value = "Este territorio ya fue tomado"
                    return@launch
                }
            }

            // 3. generar zona alrededor del jugador
            val zonaGenerada = generarZonaClan(lastLoc)

            // 4. generar ID válido
            val clanId = nombre
                .trim()
                .lowercase()
                .replace(" ", "_")
                .replace(Regex("[^a-zA-Z0-9_]"), "")

            // 5. crear clan
            val nuevoClan = mapOf(
                "id" to clanId,
                "nombre" to nombre,
                "descripcion" to descripcion,
                "fuerza" to 0,
                "miembros" to mapOf(uid to true),
                "zona" to zonaGenerada
            )

            // 6. guardar en Firebase
            db.child(clanId)
                .setValue(nuevoClan)
                .addOnSuccessListener {

                    usersRef.child(uid).child("clanid").setValue(clanId)
                    usersRef.child(uid).child("clanRole").setValue("lider")

                    _mensaje.value = "Clan creado exitosamente"
                }
                .addOnFailureListener {
                    _mensaje.value = "Error al crear el clan"
                }
        }
    }



    fun unirseAClan(clan: Clan, user: MyUserState) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.child(clan.id)
            .child("miembros")
            .child(uid)
            .setValue(true)
            .addOnSuccessListener {

                usersRef.child(uid).child("clanid").setValue(clan.id)
                usersRef.child(uid).child("clanRole").setValue("miembro")

                _mensaje.value = "Te uniste al clan ${clan.nombre}"
            }
    }

    fun setMensaje(text: String) {
        _mensaje.value = text
    }

    suspend fun obtenerLastLocation(uid: String): Zona? {
        return suspendCancellableCoroutine { cont ->
            usersRef.child(uid).child("lastLocation")
                .get()
                .addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        val loc = snap.getValue(Zona::class.java)
                        cont.resume(loc)
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

    private fun generarZonaClan(center: Zona, radiusMeters: Double = 200.0): List<Zona> {

        val earth = 6378137.0

        val dLat = radiusMeters / earth
        val dLng = radiusMeters / (earth * kotlin.math.cos(Math.toRadians(center.latitude)))

        return listOf(
            Zona(center.latitude + Math.toDegrees(dLat), center.longitude - Math.toDegrees(dLng)),
            Zona(center.latitude + Math.toDegrees(dLat), center.longitude + Math.toDegrees(dLng)),
            Zona(center.latitude - Math.toDegrees(dLat), center.longitude + Math.toDegrees(dLng)),
            Zona(center.latitude - Math.toDegrees(dLat), center.longitude - Math.toDegrees(dLng))
        )
    }


    private fun estaDentroDeZona(lat: Double, lng: Double, zona: List<Zona>): Boolean {
        if (zona.size < 3) return false

        // Algoritmo ray-casting
        var intersect = false
        for (i in zona.indices) {
            val p1 = zona[i]
            val p2 = zona[(i + 1) % zona.size]

            if (((p1.longitude > lng) != (p2.longitude > lng)) &&
                (lat < (p2.latitude - p1.latitude) * (lng - p1.longitude) / (p2.longitude - p1.longitude) + p1.latitude)
            ) {
                intersect = !intersect
            }
        }
        return intersect
    }

    // --------------------------- Escuchar usuario actual ---------------------------
    fun listenCurrentUserRealtime() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        usersRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(MyUserState::class.java)
                if (user != null) _currentUser.value = user
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --------------------------- Obtener usuario por UID ---------------------------
    suspend fun getUsuarioAsync(uid: String): MyUserState? {
        return try {
            val snapshot = usersRef.child(uid).get().await()
            snapshot.getValue(MyUserState::class.java)
        } catch (e: Exception) {
            null
        }
    }





}
