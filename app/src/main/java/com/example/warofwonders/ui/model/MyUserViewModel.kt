package com.example.warofwonders.ui.model

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

val database = Firebase.database
const val pathUsers = "users/"

class MyUserViewModel(application: Application) : AndroidViewModel(application) {

    private val myRef = database.getReference(pathUsers)
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    private val _users = MutableStateFlow(listOf<MyUserState>())
    val users = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<MyUserState?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val vel: ValueEventListener = myRef.addValueEventListener(
        object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = mutableListOf<MyUserState>()
                for (child in snapshot.children) {
                    val user = child.getValue(MyUserState::class.java)
                    user?.let { newList.add(it) }
                }
                _users.value = newList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseApp", "Error al leer usuarios: ${error.message}")
            }
        }
    )

    fun registerUserWithFirebase(
        user: MyUserState,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val email = user.email.trim()
        val password = user.password.trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val userData = mapOf(
                        "name" to user.name,
                        "lastName" to user.lastName,
                        "phone" to user.phone,
                        "email" to user.email,
                        "monedas" to user.monedas,
                        "nivel" to user.nivel,
                        "experiencia" to user.experiencia,
                        "clan" to user.clanId,
                        "profileImageUrl" to user.imagen
                    )

                    myRef.child(uid).setValue(userData)
                        .addOnSuccessListener {
                            cacheUserLocally(user)
                            onSuccess()
                        }
                        .addOnFailureListener { e -> onError(e) }
                } else {
                    onError(task.exception ?: Exception("Error al crear usuario"))
                }
            }
    }

    fun saveUser(user: MyUserState) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        myRef.child(uid).setValue(user)
        cacheUserLocally(user)
    }

    fun loadCurrentUser() {

        val cachedUser = getCachedUser()
        if (cachedUser != null) {
            _currentUser.value = cachedUser
            Log.i("FirebaseApp", "Usuario cargado desde cache: ${cachedUser.name}")
        }


        val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: return

        myRef.orderByChild("email").equalTo(currentEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.children.firstOrNull()?.getValue(MyUserState::class.java)
                    user?.let {
                        _currentUser.value = it
                        cacheUserLocally(it)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseApp", "Error al obtener usuario actual: ${error.message}")
                }
            })
    }

    fun updateProfileImage(
        email: String,
        imageUri: Uri,
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("profile_images/${email}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    myRef.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val key = snapshot.children.firstOrNull()?.key ?: return
                                myRef.child(key).child("profileImageUrl").setValue(uri.toString())
                                Log.i("FirebaseApp", "Imagen actualizada correctamente para $email")


                                _currentUser.value = _currentUser.value?.copy(imagen = uri.toString())
                                _currentUser.value?.let { cacheUserLocally(it) }

                                onSuccess?.invoke()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseApp", "Error al actualizar imagen: ${error.message}")
                            }
                        })
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseApp", "Error al subir imagen: ${e.message}")
                onError?.invoke(e)
            }
    }


    private fun cacheUserLocally(user: MyUserState) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("email", user.email)
            putString("name", user.name)
            putString("team", user.clanId)
            putString("profileImageUrl", user.imagen)
            apply()
        }
    }


    private fun getCachedUser(): MyUserState? {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("email", null) ?: return null
        return MyUserState(
            email = email,
            name = prefs.getString("name", "") ?: "",
            clanId = prefs.getString("clan", "") ?: "",
            imagen = prefs.getString("foto de perfil", "") ?: ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        myRef.removeEventListener(vel)
    }
}
