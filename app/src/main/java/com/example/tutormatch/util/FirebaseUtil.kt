package com.example.tutormatch.util

import android.util.Log
import com.example.tutormatch.data.model.Utente
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object FirebaseUtil {
    private val db = FirebaseFirestore.getInstance() // Istanza di Firestore

    // Aggiunge un nuovo utente a Firestore
    fun addUserToFirestore(utente: Utente) {
        db.collection("utenti").document(utente.userId).set(utente)
            .addOnSuccessListener {
                Log.d("FirebaseUtil", "Utente aggiunto a Firestore: ${utente.userId}")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtil", "Errore nell'aggiunta dell'utente: ${e.message}")
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
                Log.e("FirebaseUtil", "Errore nel recupero dell'utente: ${it.message}")
                callback(null)
            }
    }

    // Salva il token FCM di un utente
    fun saveUserFcmToken(userId: String, token: String) {
        db.collection("utenti").document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FirebaseUtil", "Token FCM aggiornato per l'utente con ID $userId")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtil", "Errore nell'aggiornamento del token FCM: ${e.message}")
            }
    }

    // Funzione per inviare una notifica FCM tramite HTTP POST
    fun sendNotificationToUserFCM(token: String, title: String, message: String) {
        val json = """
            {
                "to": "$token",
                "notification": {
                    "title": "$title",
                    "body": "$message"
                },
                "data": {
                    "message": "$message"
                }
            }
        """.trimIndent()

        val url = "https://fcm.googleapis.com/fcm/send"
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val requestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "key=LA_TUA_SERVER_KEY")  // Inserisci la tua chiave FCM qui
            .addHeader("Content-Type", "application/json")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("FCM", "Notifica inviata con successo: ${response.body?.string()}")
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM", "Errore nell'invio della notifica: ${e.message}")
            }
        })
    }
}
