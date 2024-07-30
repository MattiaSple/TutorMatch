package com.example.tutormatch.util

import com.example.tutormatch.data.model.Utente
import com.google.firebase.firestore.FirebaseFirestore

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
}
