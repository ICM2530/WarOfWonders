package com.example.warofwonders.ui.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.State
import kotlinx.coroutines.delay


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

    private val _mostrarPopupRecursos = MutableStateFlow(false)
    val mostrarPopupRecursos = _mostrarPopupRecursos.asStateFlow()

    private val _saludFlotante = MutableStateFlow<Int?>(null)
    val saludFlotante = _saludFlotante.asStateFlow()

    private val _mensajeError = mutableStateOf("")

    val mensajeError: State<String> get() = _mensajeError

    init {
        // Cargar inventario al crear el ViewModel
        viewModelScope.launch {
            cargarInventario()
        }
    }




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

    fun asignarRecursoACriatura(criaturaId: String, recurso: Recurso) {
        val usuario = _inventario.value
        val criaturas = usuario.criaturas.toMutableList()
        val recursos = usuario.recursos.toMutableList()

        val index = criaturas.indexOfFirst { it.id == criaturaId }
        if (index == -1) return

        val criatura = criaturas[index]
        var nuevaSalud = criatura.salud
        var nuevoDano = criatura.dano
        val recursosCriatura = criatura.recursos.toMutableList()

        // Si ya tiene el recurso, no agregar de nuevo
        if (recursosCriatura.any { it.id == recurso.id }) return

        // Aplicar efectos según tipo o rareza
        when (recurso.tipo.lowercase()) {
            "armadura" -> {
                nuevaSalud += recurso.proteccion
                _saludFlotante.value = recurso.proteccion
            }
        }

        if (recurso.rareza.lowercase() == "curacion") {
            if (criatura.salud >= 100) {

                _mensajeError.value = "¡Tu salud está al máximo!"
                viewModelScope.launch {
                    delay(1000)
                    _mensajeError.value = ""
                }
                return
            }


            nuevaSalud += recurso.proteccion
            if (nuevaSalud > 100) nuevaSalud = 100
            _saludFlotante.value = recurso.proteccion
        }




        // Si la rareza indica daño, sumar al daño de la criatura
        if (recurso.rareza.lowercase() == "dano") {
            nuevoDano += recurso.dano
        }

        // Agregar recurso a la criatura y quitar del inventario
        recursosCriatura.add(recurso)
        recursos.removeIf { it.id == recurso.id }

        // Actualizar criatura
        criaturas[index] = criatura.copy(
            salud = nuevaSalud,
            dano = nuevoDano,
            recursos = recursosCriatura
        )

        // Actualizar inventario
        val actualizado = usuario.copy(
            criaturas = criaturas,
            recursos = recursos
        )
        _inventario.value = actualizado

        val uid = auth.currentUser?.uid ?: return
        usersDb.child(uid).child("criaturas").setValue(criaturas)
        usersDb.child(uid).child("recursos").setValue(recursos)

        refrescarCriaturaSeleccionada()
    }





    fun quitarArmadura(criaturaId: String, recurso: Recurso) {
        val usuario = _inventario.value
        val criaturas = usuario.criaturas.toMutableList()
        val recursos = usuario.recursos.toMutableList()

        val index = criaturas.indexOfFirst { it.id == criaturaId }
        if (index == -1) return

        val criatura = criaturas[index]
        var nuevaSalud = criatura.salud
        val recursosCriatura = criatura.recursos.toMutableList()

        recursosCriatura.removeIf { it.id == recurso.id }

        nuevaSalud -= recurso.proteccion


        if (recursos.none { it.id == recurso.id }) {
            recursos.add(recurso)
        }

        criaturas[index] = criatura.copy(
            salud = nuevaSalud,
            recursos = recursosCriatura
        )

        val actualizado = usuario.copy(
            criaturas = criaturas,
            recursos = recursos
        )
        _inventario.value = actualizado

        val uid = auth.currentUser?.uid ?: return
        usersDb.child(uid).child("criaturas").setValue(criaturas)
        usersDb.child(uid).child("recursos").setValue(recursos)


        refrescarCriaturaSeleccionada()

    }




    fun abrirPopupRecursos() {
        _mostrarPopupRecursos.value = true
    }

    fun cerrarPopupRecursos() {
        _mostrarPopupRecursos.value = false
    }

    fun limpiarFlotante() {
        _saludFlotante.value = null
    }


    fun refrescarCriaturaSeleccionada() {
        val actual = _criaturaSeleccionada.value ?: return
        val nueva = _inventario.value.criaturas.firstOrNull { it.id == actual.id }
        _criaturaSeleccionada.value = nueva
    }

    fun mostrarMensajeError(mensaje: String) {
        _mensajeError.value = mensaje
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _mensajeError.value = ""
        }
    }

    fun limpiarMensajeError() {
        _mensajeError.value = ""
    }

}