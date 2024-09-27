package com.example.tutormatch.data.model

import com.google.firebase.firestore.DocumentReference

data class Prenotazione(
    val annuncioRef: DocumentReference,        // Riferimento all'annuncio
    val fasciaCalendarioRef: DocumentReference, // Riferimento alla fascia oraria
    val studenteRef: String
)
