package com.example.tutormatch.data.model

import com.google.firebase.firestore.DocumentReference

data class Prenotazione(
    val annuncioRef: DocumentReference? = null,        // Riferimento all'annuncio
    val fasciaCalendarioRef: DocumentReference? = null, // Riferimento alla fascia oraria
    val studenteRef: String,
    val tutorRef: String
)
{
    // Costruttore senza argomenti richiesto da Firestore per la deserializzazione
    constructor() : this(null, null, "", "")
}
