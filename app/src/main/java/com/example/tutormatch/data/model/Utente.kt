package com.example.tutormatch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utente")
data class Utente(
    @PrimaryKey val email: String,
    val nome: String,
    val cognome: String,
    val password: String,
    val residenza: String,
    val via: String,
    val civico: String,
    val ruolo: Boolean)