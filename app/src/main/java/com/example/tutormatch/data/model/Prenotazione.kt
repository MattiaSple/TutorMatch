package com.example.tutormatch.data.model

import com.google.firebase.firestore.DocumentReference

data class Prenotazione(
    val annuncioRef: DocumentReference, // Riferimento all'annuncio
    val id_fascia_calendario: DocumentReference, // Riferimento alla fascia oraria
    val studenteRef: DocumentReference // Riferimento allo studente che ha prenotato
)
