package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Prenotazione
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class PrenotazioneViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val prenotazioniCollection = db.collection("prenotazioni")

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // Funzione per prenotare una fascia oraria specifica usando solo i riferimenti
    fun prenotaFasciaOraria(
        annuncioRef: DocumentReference, // Riferimento all'annuncio
        id_fascia_calendario: DocumentReference, // Riferimento alla fascia oraria
        studenteRef: DocumentReference // Riferimento allo studente
    ) {
        // Creiamo la prenotazione con i riferimenti essenziali
        val nuovaPrenotazione = Prenotazione(
            annuncioRef = annuncioRef,
            id_fascia_calendario = id_fascia_calendario,
            studenteRef = studenteRef
        )

        // Salva la prenotazione su Firestore
        prenotazioniCollection.add(nuovaPrenotazione)
            .addOnSuccessListener {
                // Successo: aggiorna lo stato della fascia oraria e mostra messaggio
                aggiornaStatoFasciaOraria(id_fascia_calendario)
                _message.value = "Prenotazione effettuata con successo!"
            }
            .addOnFailureListener { e ->
                _message.value = "Errore durante la prenotazione: ${e.message}"
            }
    }

    // Funzione per aggiornare lo stato della fascia oraria nel calendario (prenotata)
    private fun aggiornaStatoFasciaOraria(id_fascia_calendario: DocumentReference) {
        id_fascia_calendario.update("stato_pren", true)
            .addOnSuccessListener {
                _message.value = "Fascia oraria aggiornata con successo."
            }
            .addOnFailureListener { e ->
                _message.value = "Errore nell'aggiornamento della fascia oraria: ${e.message}"
            }
    }

}
