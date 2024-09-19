package com.example.tutormatch.data.model

import com.google.firebase.firestore.GeoPoint

data class Utente(
    val userId: String,
    val email: String,
    val nome: String,
    val cognome: String,
    val residenza: String,
    val via: String,
    val cap: String,
    val ruolo: Boolean = false, // true se tutor, false se studente
) {
    // Costruttore senza argomenti richiesto per Firestore
    constructor() : this("", "", "", "", "", "", "", false)
}
