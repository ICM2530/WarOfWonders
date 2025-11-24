package com.example.warofwonders.ui.model


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ShopViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference

    private val _recursos = MutableStateFlow<List<Recurso>>(emptyList())
    val recursos: StateFlow<List<Recurso>> = _recursos

    private val _mensajeCompra = MutableStateFlow<String?>(null)
    val mensajeCompra: StateFlow<String?> = _mensajeCompra



    fun cargarRecursos() {
        viewModelScope.launch {
            try {
                val snapshot = db.child("recursos").get().await()
                val lista = snapshot.children.mapNotNull { it.getValue(Recurso::class.java) }
                _recursos.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
                _recursos.value = emptyList()
            }
        }
    }

    fun comprarRecurso(
        recurso: Recurso,
        usuario: MyUserState,
        inventarioVM: InventarioViewModel
    ) {
        if (inventarioVM.tieneRecurso(recurso.nombre)) {
            _mensajeCompra.value = "Ya tienes este artículo"
            return
        }

        if (usuario.coins < recurso.precio) {
            _mensajeCompra.value = "No tienes suficientes monedas"
            return
        }

        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val userRef = db.child("users").child(uid)

                val nuevasCoins = usuario.coins - recurso.precio
                userRef.child("coins").setValue(nuevasCoins).await()

                inventarioVM.agregarRecurso(recurso)

                _mensajeCompra.value = "Compraste ${recurso.nombre} por ${recurso.precio} monedas"
            } catch (e: Exception) {
                e.printStackTrace()
                _mensajeCompra.value = "Error al procesar la compra"
            }
        }
    }





    fun limpiarMensaje() {
        _mensajeCompra.value = null
    }
}
