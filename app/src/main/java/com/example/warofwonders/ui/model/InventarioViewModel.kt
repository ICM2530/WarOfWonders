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

    private val _criaturaSeleccionada = MutableStateFlow<Criatura?>(null)
    val criaturaSeleccionada = _criaturaSeleccionada.asStateFlow()

    private val _mostrarPopup = MutableStateFlow(false)
    val mostrarPopup = _mostrarPopup.asStateFlow()


    fun seleccionarCriatura(criatura: Criatura) {
        _criaturaSeleccionada.value = criatura
        _mostrarPopup.value = true
    }

    fun cerrarPopup() {
        _mostrarPopup.value = false
    }

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

    suspend fun agregarCriatura(criatura: Criatura) {
        val uid = auth.currentUser?.uid ?: return

        val nuevaCriatura = criatura.copy(
            id = System.currentTimeMillis().toString()
        )

        val nuevasCriaturas = _inventario.value.criaturas.toMutableList().apply {
            add(nuevaCriatura)
        }

        val actualizado = _inventario.value.copy(criaturas = nuevasCriaturas)
        _inventario.value = actualizado

        usersDb.child(uid).child("criaturas").setValue(nuevasCriaturas)
    }


    // AGREGAR RECURSO AL INVENTARIO
    suspend fun agregarRecurso(recurso: Recurso) {
        val uid = auth.currentUser?.uid ?: return

        val nuevoRecurso = recurso.copy(
            id = System.currentTimeMillis().toString()
        )

        val nuevosRecursos = _inventario.value.recursos.toMutableList().apply {
            add(nuevoRecurso)
        }

        val actualizado = _inventario.value.copy(recursos = nuevosRecursos)
        _inventario.value = actualizado

        usersDb.child(uid).child("recursos").setValue(nuevosRecursos)
    }

    //inventarioVM.agregarRecurso(recursoSeleccionado)


    fun tieneRecurso(nombreRecurso: String): Boolean {
        return _inventario.value.recursos.any { it.nombre == nombreRecurso }
    }




}
