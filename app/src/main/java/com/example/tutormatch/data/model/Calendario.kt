package com.example.tutormatch.data.model

import com.google.firebase.firestore.DocumentReference
import java.util.Date

data class Calendario(
    val tutorRef: DocumentReference? = null, // Cambiato a nullable
    val data: Date = Date(),
    val oraInizio: String = "",
    val oraFine: String = "",
    val statoPren: Boolean = false,
    val studenteRef: String = ""
) {
    // Costruttore senza argomenti richiesto per la deserializzazione di Firestore
    constructor() : this(null, Date(), "", "", false,"")
}
