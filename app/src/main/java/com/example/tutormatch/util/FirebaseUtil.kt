package com.example.tutormatch.util

import android.util.Log
import com.example.tutormatch.data.model.Utente
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object FirebaseUtil {
    private const val TAG = "FirebaseUtil"
    private val db = FirebaseFirestore.getInstance() // Istanza di Firestore

    // Aggiunge un nuovo utente a Firestore
    fun addUserToFirestore(utente: Utente) {
        if (utente.userId.isBlank()) {
            Log.e(TAG, "L'ID utente è vuoto, impossibile aggiungere l'utente.")
            return
        }

        db.collection("utenti").document(utente.userId).set(utente)
            .addOnSuccessListener {
                Log.d(TAG, "Utente aggiunto a Firestore: ${utente.userId}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Errore nell'aggiunta dell'utente: ${e.message}")
            }
    }

    // Cerca un utente in Firestore tramite email e restituisce l'userId
    fun getUserIdByEmail(email: String, callback: (String?) -> Unit) {
        if (email.isBlank()) {
            Log.e(TAG, "L'email è vuota, impossibile recuperare l'userId.")
            callback(null)
            return
        }

        db.collection("utenti").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userId = documents.first().getString("userId")
                    Log.d(TAG, "UserId trovato per l'email $email: $userId")
                    callback(userId)
                } else {
                    Log.e(TAG, "Nessun utente trovato per l'email: $email")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Errore nel recupero dell'userId: ${e.message}")
                callback(null)
            }
    }

    // Recupera i dati di un utente da Firestore tramite l'email
    fun getUserFromFirestoreByEmail(email: String, callback: (Utente?) -> Unit) {
        getUserIdByEmail(email) { userId ->
            if (userId == null) {
                Log.e(TAG, "UserId non trovato per l'email: $email")
                callback(null)
                return@getUserIdByEmail
            }

            getUserFromFirestore(userId, callback)
        }
    }

    // Recupera i dati di un utente da Firestore tramite userId
    fun getUserFromFirestore(userId: String, callback: (Utente?) -> Unit) {
        if (userId.isBlank()) {
            Log.e(TAG, "L'ID utente è vuoto, impossibile recuperare i dati.")
            callback(null)
            return
        }

        db.collection("utenti").document(userId).get()
            .addOnSuccessListener { document ->
                val utente = document.toObject(Utente::class.java)
                if (utente != null) {
                    Log.d(TAG, "Utente trovato: ${utente.email}")
                    callback(utente)
                } else {
                    Log.e(TAG, "Nessun utente trovato per l'ID: $userId")
                    callback(null)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Errore nel recupero dell'utente: ${it.message}")
                callback(null)
            }
    }

    // Salva il token FCM di un utente tramite email
    fun saveUserFcmTokenByEmail(email: String, token: String) {
        getUserIdByEmail(email) { userId ->
            if (userId == null) {
                Log.e(TAG, "Impossibile aggiornare il token FCM. Nessun userId trovato per l'email: $email")
                return@getUserIdByEmail
            }

            saveUserFcmToken(userId, token)
        }
    }

    // Salva il token FCM di un utente tramite userId
    fun saveUserFcmToken(userId: String, token: String) {
        if (userId.isBlank() || token.isBlank()) {
            Log.e(TAG, "Impossibile aggiornare il token FCM. ID utente o token vuoto.")
            return
        }

        db.collection("utenti").document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "Token FCM aggiornato per l'utente con ID $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Errore nell'aggiornamento del token FCM: ${e.message}")
            }
    }

    // Funzione per inviare una notifica FCM tramite HTTP POST
    fun sendNotificationToUserFCM(token: String, title: String, message: String) {
        if (token.isBlank()) {
            Log.e(TAG, "Token FCM vuoto. Impossibile inviare notifica.")
            return
        }

        if (title.isBlank() || message.isBlank()) {
            Log.e(TAG, "Titolo o messaggio vuoto. Impossibile inviare notifica.")
            return
        }

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
            .addHeader("Authorization", "key=4adafc388f0d508c09c6fc776329b351780e0fd4")  // Inserisci la tua chiave FCM qui
            .addHeader("Content-Type", "application/json")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // Verifica se la risposta è stata un successo
                if (response.isSuccessful) {
                    Log.d(TAG, "Notifica inviata con successo. Codice di stato: ${response.code}")
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Risposta FCM: $responseBody")
                } else {
                    Log.e(TAG, "Errore nella risposta FCM. Codice di stato: ${response.code}")
                    val errorBody = response.body?.string()
                    Log.e(TAG, "Corpo della risposta d'errore: $errorBody")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Log di errore in caso di fallimento della richiesta
                Log.e(TAG, "Errore nell'invio della notifica: ${e.message}")
                e.printStackTrace()  // Log completo dell'errore
            }
        })

    }
}
