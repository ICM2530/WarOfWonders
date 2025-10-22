package com.example.warofwonders.ui.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Usar esto después para mostrar usuarios en línea y hacer combates, intercambios, etc
val database = Firebase.database
const val pathUsers = "users/"

class MyUserViewModel : ViewModel() {

    private val myRef = database.getReference(pathUsers)
    private val _users = MutableStateFlow(listOf<MyUserState>())
    val users = _users.asStateFlow()

    private val vel: ValueEventListener = myRef.addValueEventListener(
        object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = mutableListOf<MyUserState>()
                for (child in snapshot.children) {
                    val user = child.getValue<MyUserState>()
                    user?.let { newList.add(it) }
                }
                _users.value = newList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseApp", error.toString())
            }
        }
    )

    fun saveUser(user: MyUserState) {
        val key = myRef.push().key ?: return

        val cleanUser = mapOf(
            "name" to user.name,
            "lastName" to user.lastName,
            "phone" to user.phone,
            "email" to user.email,
            "password" to user.password
        )

        myRef.child(key).setValue(cleanUser)
    }

    override fun onCleared() {
        super.onCleared()
        myRef.removeEventListener(vel)
    }
}