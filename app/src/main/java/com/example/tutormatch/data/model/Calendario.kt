package com.example.tutormatch.data.model

import com.google.firebase.firestore.DocumentReference
import java.util.Date

data class Calendario(
    val tutorRef: DocumentReference, // Riferimento al documento utente del tutor
    val data: Date,
    val oraInizio: String, // Ora di inizio della fascia oraria
    val oraFine: String, // Ora di fine della fascia oraria
    val stato_pren: Boolean = false // Stato della prenotazione
)
