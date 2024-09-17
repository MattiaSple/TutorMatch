package com.example.tutormatch.util

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
    fun saveUserFcmToken(userId: String, token: String) {
        db.collection("utenti").document(userId).update("fcmToken", token)
            .addOnSuccessListener {
                // Token aggiornato con successo
            }
            .addOnFailureListener { e ->
                // Errore nell'aggiornamento del token
            }
    }

    // Metodo per inviare una notifica (puoi chiamarlo nel momento in cui invii un nuovo messaggio)
    fun sendNotificationToUser(userId: String, message: String) {
        // Recupera il token dell'utente dal Firestore
        getUserFromFirestore(userId) { utente ->
            utente?.let {
                val fcmToken = it.fcmToken
                if (fcmToken != null) {
                    // Usa il token per inviare una notifica tramite FCM
                    FirebaseMessaging.getInstance().send(
                        RemoteMessage.Builder(fcmToken)
                            .setMessageId("message_${System.currentTimeMillis()}")
                            .addData("message", message)
                            .build()
                    )
                }
            }
        }
    }
}
