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
        // Quando arriva un messaggio da FCM
        Log.d(TAG, "Messaggio ricevuto da: ${remoteMessage.from}")

        // Se il messaggio contiene dati
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Dati del messaggio: ${remoteMessage.data}")
            // Gestisci i dati del messaggio
            showNotification(remoteMessage.data["title"], remoteMessage.data["body"])
        }

        // Se il messaggio contiene una notifica
        remoteMessage.notification?.let {
            Log.d(TAG, "Messaggio di notifica: ${it.body}")
            showNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Nuovo token FCM: $token")
        // Qui puoi salvare il nuovo token per l'utente nel Firestore o nel backend
        FirebaseUtil.saveUserFcmToken(FirebaseAuth.getInstance().currentUser?.email ?: "", token)
    }

    // Funzione per mostrare la notifica
    private fun showNotification(title: String?, message: String?) {
        // Verifica se l'app ha il permesso di inviare notifiche
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Se il permesso non è stato ancora concesso, esci dalla funzione
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
            .setSmallIcon(R.drawable.ic_notification) // Usa un'icona adeguata
            .setContentTitle(title ?: "Nuovo messaggio")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Mostra la notifica
        with(NotificationManagerCompat.from(this)) {
            notify(0, notificationBuilder.build())
        }
    }

}
