package com.example.warofwonders.ui.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

enum class TipoCriatura { FRIO, CALOR, MEDIO, NOCHE, DIA, PRESION }

class InventarioViewModel : ViewModel() {

    private val usersDb = FirebaseDatabase.getInstance().getReference("users")
    private val catalogoDb = FirebaseDatabase.getInstance().getReference("criaturas_disponibles")
    private val auth = FirebaseAuth.getInstance()

    // Estado del usuario con inventario
    private val _inventario = MutableStateFlow(MyUserState())
    val inventario = _inventario.asStateFlow()

    // Estado del catálogo global
    private val _catalogo = MutableStateFlow<List<Criatura>>(emptyList())
    val catalogo = _catalogo.asStateFlow()

    // Cargar inventario del usuario
    suspend fun cargarInventario() {
        val uid = auth.currentUser?.uid ?: return
        val snapshot = usersDb.child(uid).get().await()
        val user = snapshot.getValue(MyUserState::class.java)
        user?.let {
            _inventario.value = it
        }
    }

    // Cargar catálogo global de criaturas (CRIATURAS SALVAJES)
    suspend fun cargarCatalogoGlobal() {
        val snapshot = catalogoDb.get().await()
        val lista = snapshot.children.mapNotNull { it.getValue(Criatura::class.java) }
        _catalogo.value = lista
    }

    // Obtener criaturas según el sensor
    suspend fun obtenerCriaturasPorTipo(tipo: TipoCriatura): List<Criatura> {
        if (_catalogo.value.isEmpty()) cargarCatalogoGlobal()

        return _catalogo.value.filter { it.tipo == tipo.name }
    }

    // AGREGAR CRIATURA AL INVENTARIO DEL USUARIO
    suspend fun agregarCriatura(nombre: String, tipo: TipoCriatura, imagen: String) {
        val uid = auth.currentUser?.uid ?: return

        val nuevaCriatura = Criatura(
            id = System.currentTimeMillis().toString(),
            nombre = nombre,
            tipo = tipo.name,
            salud = 100,
            dano = 10,
            velocidad = 5,
            poder = 20,
            imagen = imagen
        )

        val nuevasCriaturas = _inventario.value.criaturas.toMutableList().apply {
            add(nuevaCriatura)
        }

        val actualizado = _inventario.value.copy(criaturas = nuevasCriaturas)
        _inventario.value = actualizado

        usersDb.child(uid).child("criaturas").setValue(nuevasCriaturas)
    }

    // AGREGAR RECURSO AL INVENTARIO
    suspend fun agregarRecurso(
        nombre: String,
        tipo: String,
        material: String,
        imagen: String
    ) {
        val uid = auth.currentUser?.uid ?: return

        val nuevoRecurso = Recurso(
            id = System.currentTimeMillis().toString(),
            nombre = nombre,
            tipo = tipo,
            material = material,
            imagen = imagen,
            rareza = "común",
            protección = 10,
            daño = 5,
            velocidad = 3,
            precio = 50
        )

        val nuevosRecursos = _inventario.value.recursos.toMutableList().apply {
            add(nuevoRecurso)
        }

        val actualizado = _inventario.value.copy(recursos = nuevosRecursos)
        _inventario.value = actualizado

        usersDb.child(uid).child("recursos").setValue(nuevosRecursos)
    }


}
