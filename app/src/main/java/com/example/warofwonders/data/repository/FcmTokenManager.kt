package com.example.warofwonders.data.repository

import com.example.warofwonders.ui.model.database
import com.example.warofwonders.ui.model.pathUsers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {

    fun updateTokenIfLoggedIn() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) return@addOnCompleteListener
                val token = task.result ?: return@addOnCompleteListener
                val usersRef = database.getReference(pathUsers)
                usersRef.child(uid).child("fcmToken").setValue(token)
            }
    }
}