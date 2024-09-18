package com.example.tutormatch.util

import android.util.Log
import com.example.tutormatch.data.model.Utente
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

// Oggetto per gestire le operazioni Firestore
object FirebaseUtil {
    private val db = FirebaseFirestore.getInstance() // Istanza di Firestore

    // Aggiunge un nuovo utente a Firestore
    fun addUserToFirestore(utente: Utente) {
        db.collection("utenti").document(utente.userId).set(utente)
            .addOnSuccessListener {
                // Utente aggiunto con successo
            }
            .addOnFailureListener { e ->
                // Errore nell'aggiunta dell'utente
            }
    }

    // Recupera i dati di un utente da Firestore
    fun getUserFromFirestore(userId: String, callback: (Utente?) -> Unit) {
        db.collection("utenti").document(userId).get()
            .addOnSuccessListener { document ->
                val utente = document.toObject(Utente::class.java)
                callback(utente)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    // Aggiungi un metodo per salvare il token FCM di un utente
    fun saveUserFcmToken(email: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("utenti").document(email)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FirebaseUtil", "Token FCM aggiornato per l'utente con email $email")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtil", "Errore nell'aggiornamento del token FCM: ${e.message}")
            }
    }

    // Aggiungi una funzione per inviare la notifica cercando il token FCM tramite l'email
    fun sendNotificationToUser(email: String, message: String) {
        db.collection("utenti")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val token = documents.first().getString("fcmToken")
                    if (token != null) {
                        Log.d("FirebaseUtil", "Token FCM trovato: $token")
                        sendFCMNotification(token, message)
                    } else {
                        Log.e("FirebaseUtil", "Nessun token FCM trovato per l'utente con email $email")
                    }
                } else {
                    Log.e("FirebaseUtil", "Nessun documento trovato per l'utente con email $email")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtil", "Errore durante il recupero del token FCM: ${e.message}")
            }
    }

    // Funzione per inviare la notifica tramite FCM
    private fun sendFCMNotification(token: String, message: String) {
        val notificationData = mapOf(
            "message" to message,
            "title" to "Nuovo messaggio",
            "body" to message
        )

        // Purtroppo, non c'è un callback diretto su RemoteMessage per verificare il successo
        FirebaseMessaging.getInstance().send(
            RemoteMessage.Builder("$token@fcm.googleapis.com")
                .setMessageId("message_${System.currentTimeMillis()}")
                .setData(notificationData)
                .build()
        )

        Log.d("FirebaseUtil", "Notifica inviata a $token con il messaggio: $message")
    }
}
