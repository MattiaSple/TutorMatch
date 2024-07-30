package com.example.tutormatch.data.model

data class Utente(
    val userId: String = "",
    val email: String = "",
    val nome: String = "",
    val cognome: String = "",
    val residenza: String = "",
    val via: String = "",
    val civico: String = "",
    val ruolo: Boolean = false // true se tutor, false se studente
)
