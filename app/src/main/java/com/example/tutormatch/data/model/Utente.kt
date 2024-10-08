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
    val feedback: MutableList<String> = mutableListOf(), // ID dei tutor da valutare (solo per studenti)
    val valutazioni: MutableList<Double> = mutableListOf() // Valutazioni ricevute dai tutor (solo per tutor)
) {
    // Costruttore senza argomenti richiesto per Firestore
    constructor() : this("", "", "", "", "", "", "", false)
}
