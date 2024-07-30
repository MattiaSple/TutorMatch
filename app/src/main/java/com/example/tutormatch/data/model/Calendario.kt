package com.example.tutormatch.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Calendario(
    val tutorRef: DocumentReference, // Riferimento al documento utente del tutor
    val data: Timestamp, // Timestamp per la data e l'ora
    val stato_pren: Boolean = false
)
