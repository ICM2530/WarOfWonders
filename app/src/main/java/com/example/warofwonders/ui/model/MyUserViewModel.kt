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
        state: MyUserState,
        profileImageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val email = state.email.trim()
        val password = state.password.trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onError(task.exception ?: Exception("Error al crear usuario"))
                    return@addOnCompleteListener
                }

                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                fun saveUser(profileUrl: String) {
                    val userData = mapOf(
                        "name" to state.name,
                        "lastName" to state.lastName,
                        "phone" to state.phone,
                        "email" to state.email,
                        "coins" to state.coins,
                        "level" to state.level,
                        "xp" to state.xp,
                        "team" to state.team,
                        "profileImageUrl" to profileUrl,
                        "active" to true
                    )

                    myRef.child(uid).setValue(userData)
                        .addOnSuccessListener {
                            val cached = state.copy(profileImageUrl = profileUrl)
                            cacheUserLocally(cached)
                            _currentUser.value = cached
                            onSuccess()
                        }
                        .addOnFailureListener { e -> onError(e) }
                }

                if (profileImageUri != null) {
                    val storageRef = Firebase.storage.reference
                        .child("profile_images/$uid.jpg")

                    storageRef.putFile(profileImageUri)
                        .continueWithTask { storageRef.downloadUrl }
                        .addOnSuccessListener { uri ->
                            saveUser(uri.toString())
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseApp", "Error al subir imagen de registro: ${e.message}")
                            saveUser("")
                        }
                } else {
                    saveUser("")
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
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("profile_images/${uid}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    myRef.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val key = snapshot.children.firstOrNull()?.key ?: return
                                myRef.child(key).child("profileImageUrl").setValue(uri.toString())
                                Log.i("FirebaseApp", "Imagen actualizada correctamente para $email")

                                _currentUser.value =
                                    _currentUser.value?.copy(profileImageUrl = uri.toString())
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
            putString("team", user.team)
            putString("profileImageUrl", user.profileImageUrl)
            apply()
        }
    }

    private fun getCachedUser(): MyUserState? {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("email", null) ?: return null
        return MyUserState(
            email = email,
            name = prefs.getString("name", "") ?: "",
            team = prefs.getString("team", "") ?: "",
            profileImageUrl = prefs.getString("profileImageUrl", "") ?: ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        myRef.removeEventListener(vel)
    }

    fun loadUser(uid: String) {
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .get()
            .addOnSuccessListener { snap ->
                val user = snap.getValue(MyUserState::class.java)
                _currentUser.value = user
            }
    }

}