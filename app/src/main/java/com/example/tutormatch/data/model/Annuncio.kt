package com.example.tutormatch.data.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint

data class Annuncio(
    var id: String = "",
    val descrizione: String = "",
    val materia: String = "",
    val mod_on: Boolean = false,
    val mod_pres: Boolean = false,
    val posizione: GeoPoint = GeoPoint(0.0, 0.0),
    val prezzo: String = "",
    val tutor: DocumentReference? = null // Cambiato a nullable
) {
    // Costruttore senza argomenti richiesto per la deserializzazione di Firestore
    constructor() : this("","", "", false, false, GeoPoint(0.0, 0.0), "", null)

}