package com.example.tutormatch.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tutormatch.R
import com.example.tutormatch.ui.view.activity.HomeActivity
import com.example.tutormatch.util.FirebaseUtil // Utilizziamo FirebaseUtil per gestire il token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Qui puoi gestire la notifica quando arriva
        Log.d("FCM", "Messaggio ricevuto: ${remoteMessage.data}")

        // Mostra la notifica localmente
        showNotification(remoteMessage.data["title"], remoteMessage.data["body"])
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Token aggiornato: $token")
        // Salva il nuovo token per l'utente
        FirebaseUtil.saveUserFcmToken(FirebaseAuth.getInstance().currentUser?.email ?: "", token)
    }

    private fun showNotification(title: String?, message: String?) {
        val notificationBuilder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}

