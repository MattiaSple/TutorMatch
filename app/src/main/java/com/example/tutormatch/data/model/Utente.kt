package com.example.tutormatch.data.model

data class Utente(
    val userId: String,
    val email: String,
    val nome: String,
    val cognome: String,
    val residenza: String,
    val via: String,
    val cap: String,
    val ruolo: Boolean = false, // true se tutor, false se studente
    val tutorDaValutare: MutableList<String> = mutableListOf(),
    val feedback: MutableList<Int> = mutableListOf()
) {
    // Costruttore senza argomenti richiesto per Firestore
    constructor() : this("", "", "", "", "", "", "", false)
}
