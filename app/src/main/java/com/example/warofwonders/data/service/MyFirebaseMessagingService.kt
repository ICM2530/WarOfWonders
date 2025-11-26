package com.example.warofwonders.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.warofwonders.MainActivity
import com.example.warofwonders.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest
import android.content.pm.PackageManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "friend_requests_channel"
        private const val TAG = "MyFCM"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio MyFirebaseMessagingService creado")
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken llamado, token=$token")

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "onNewToken currentUid=$currentUid")

        if (currentUid == null) {
            Log.d(TAG, "onNewToken: no hay usuario logueado, NO se guarda token")
            return
        }

        FirebaseDatabase.getInstance()
            .reference.child("users")
            .child(currentUid)
            .child("fcmToken")
            .setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, "onNewToken: token guardado correctamente en Realtime DB")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "onNewToken: error guardando token en Realtime DB", e)
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(
            TAG,
            "onMessageReceived: from=${message.from}, data=${message.data}, notifTitle=${message.notification?.title}, notifBody=${message.notification?.body}"
        )

        val title = message.notification?.title ?: message.data["title"] ?: "War of Wonders"
        val body = message.notification?.body ?: message.data["body"] ?: "Notificación"

        Log.d(TAG, "onMessageReceived: usando title='$title', body='$body'")

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        Log.d(TAG, "showNotification llamado con title='$title', body='$body'")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            Log.d(TAG, "showNotification: POST_NOTIFICATIONS granted=$hasPermission")

            if (!hasPermission) {
                Log.d(TAG, "showNotification: NO hay permiso, no se muestra notificación")
                return
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            flags
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val id = System.currentTimeMillis().toInt()
        Log.d(TAG, "showNotification: notificando con id=$id")

        with(NotificationManagerCompat.from(this)) {
            notify(id, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel llamado")
            val name = "Solicitudes de amistad"
            val descriptionText = "Notificaciones de amistad"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "createNotificationChannel: canal creado/actualizado")
        } else {
            Log.d(TAG, "createNotificationChannel: no necesario (< O)")
        }
    }
}