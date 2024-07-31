package com.example.tutormatch.data.model

import com.google.firebase.firestore.DocumentReference

data class Prenotazione(
    val descrizione: String = "",
    val materia: String = "",
    val mod_on: Boolean = false,
    val mod_pres: Boolean = false,
    val prezzo: Int = 0,
    val tutor: DocumentReference
)
