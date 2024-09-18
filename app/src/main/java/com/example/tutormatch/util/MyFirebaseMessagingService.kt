package com.example.tutormatch.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.tutormatch.R
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "tutormatch_notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Messaggio ricevuto da: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]

        if (title != null && body != null) {
            showNotification(title, body)
        }
    }

    override fun onNewToken(token: String) {
        FirebaseAuth.getInstance().currentUser?.email?.let { email ->
            Log.d(TAG, "Nuovo token FCM: $token")
            FirebaseUtil.saveUserFcmToken(email, token)
        } ?: Log.e(TAG, "Nessun utente autenticato. Non posso salvare il token.")
    }

    // Funzione per mostrare la notifica
    private fun showNotification(title: String?, message: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permesso di inviare notifiche non concesso")
                return
            }
        }

        // Crea il NotificationChannel se è Android 8.0 o successivo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "TutorMatch Notifications"
            val descriptionText = "Canale per notifiche di TutorMatch"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Costruisci la notifica
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: "Nuovo messaggio")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            notify(0, notificationBuilder.build())
        }
    }
}
