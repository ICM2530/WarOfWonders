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

val database = Firebase.database
const val pathUsers = "users/"

class MyUserViewModel: ViewModel() {
    val myRef = database.getReference(pathUsers)
    val usersPrivate = MutableStateFlow(listOf<MyUserState>())
    val users = usersPrivate.asStateFlow()
    val vel: ValueEventListener = myRef.addValueEventListener(
        object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = mutableListOf<MyUserState>()
                for(child in snapshot.children) {
                    val user = child.getValue<MyUserState>()
                    user?.let {
                        newList.add(user)
                    }
                }
                usersPrivate.value = newList
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseApp", error.toString())
            }
        }
    )

    override fun onCleared() {
        super.onCleared()
        myRef.removeEventListener(vel)
    }
}