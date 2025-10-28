package com.example.warofwonders.ui.model

import androidx.lifecycle.ViewModel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TipoCriatura { FRIO, CALOR, MEDIO, DIA, NOCHE, PESION }

class InventarioViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().getReference("users")
    private val auth = FirebaseAuth.getInstance()

    private val _inventario = MutableStateFlow(MyUserState())
    val inventario = _inventario.asStateFlow()

    init {
        // Cargar el inventario del usuario actual al iniciar
        val uid = auth.currentUser?.uid
        if (uid != null) {
            cargarInventario(uid)
        }
    }


    // Cargar inventario del usuario desde Firebase

    private fun cargarInventario(uid: String) {
        db.child(uid).get().addOnSuccessListener { snapshot ->
            val jugador = snapshot.getValue(MyUserState::class.java)
            if (jugador != null) {
                _inventario.value = jugador
            }
        }.addOnFailureListener {
            println(" Error al cargar inventario: ${it.message}")
        }
    }

    // Guardar inventario en Firebase

    private fun guardarInventario(jugador: MyUserState) {
        val uid = auth.currentUser?.uid ?: return
        db.child(uid).setValue(jugador)
            .addOnSuccessListener { println("Inventario actualizado") }
            .addOnFailureListener { e -> println("Error al guardar inventario: ${e.message}") }
    }


    // Agregar Criatura

    fun agregarCriatura(nombre: String, tipo: TipoCriatura, condiciones: String, imagen: String) {
        val criatura = Criatura(
            id = System.currentTimeMillis().toString(),
            nombre = nombre,
            tipo = tipo.name,
            imagen = imagen
        )

        val actualizado = _inventario.value.copy(
            criaturas = _inventario.value.criaturas.toMutableList().apply { add(criatura) }
        )
        _inventario.value = actualizado
        guardarInventario(actualizado)
    }


    //  Agregar Recurso

    fun agregarRecurso(nombre: String, tipo: String, material: String, imagen: String) {
        val recurso = Recurso(
            id = System.currentTimeMillis().toString(),
            nombre = nombre,
            tipo = tipo,
            material = material,
            imagen = imagen
        )

        val actualizado = _inventario.value.copy(
            recursos = _inventario.value.recursos.toMutableList().apply { add(recurso) }
        )
        _inventario.value = actualizado
        guardarInventario(actualizado)
    }


    //  Eliminar Criatura o Recurso

    fun eliminarCriatura(id: String) {
        val actualizado = _inventario.value.copy(
            criaturas = _inventario.value.criaturas.filterNot { it.id == id }
        )
        _inventario.value = actualizado
        guardarInventario(actualizado)
    }

    fun eliminarRecurso(id: String) {
        val actualizado = _inventario.value.copy(
            recursos = _inventario.value.recursos.filterNot { it.id == id }
        )
        _inventario.value = actualizado
        guardarInventario(actualizado)
    }
}
