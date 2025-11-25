package com.example.warofwonders.data.repository

import android.util.Log
import com.example.warofwonders.ui.model.database
import com.example.warofwonders.ui.model.pathUsers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToDatabase(token)
    }

    private fun saveTokenToDatabase(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = database.getReference(pathUsers)
        usersRef.child(uid).child("fcmToken").setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Mensaje recibido: ${message.data}")
    }
}