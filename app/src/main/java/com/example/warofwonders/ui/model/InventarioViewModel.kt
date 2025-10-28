package com.example.warofwonders.ui.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

enum class TipoCriatura { FRIO, CALOR, MEDIO, NOCHE, DIA, PRESION }

class InventarioViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().getReference("users")
    private val auth = FirebaseAuth.getInstance()

    // Estado del usuario con inventario
    private val _inventario = MutableStateFlow(MyUserState())
    val inventario = _inventario.asStateFlow()

    // Cargar inventario desde Firebase
    suspend fun cargarInventario() {
        val uid = auth.currentUser?.uid ?: return
        val snapshot = db.child(uid).get().await()
        val user = snapshot.getValue(MyUserState::class.java)
        user?.let {
            _inventario.value = it
        }
    }


    //AGREGAR CRIATURA

    suspend fun agregarCriatura(nombre: String, tipo: TipoCriatura, imagen: String) {
        val uid = auth.currentUser?.uid ?: return

        val nuevaCriatura = Criatura(
            id = System.currentTimeMillis().toString(),
            nombre = nombre,
            tipo = tipo.name,
            salud = 100,
            daño = 10,
            velocidad = 5,
            poder = 20,
            imagen = imagen
        )

        val nuevasCriaturas = _inventario.value.criaturas.toMutableList().apply {
            add(nuevaCriatura)
        }

        val actualizado = _inventario.value.copy(criaturas = nuevasCriaturas)
        _inventario.value = actualizado

        db.child(uid).child("criaturas").setValue(nuevasCriaturas)
    }

    //AGREGAR RECURSO

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

        db.child(uid).child("recursos").setValue(nuevosRecursos)
    }
}
