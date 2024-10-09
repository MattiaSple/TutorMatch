package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.util.FirebaseUtil


class PrenotazioneViewModel : ViewModel() {

    // Cambia da LiveData a una semplice variabile privata non osservata
    private var listaCreaPrenotazioni: List<Calendario> = emptyList()

    // LiveData per gestire i messaggi di notifica
    private val _notificaPrenotazione = MutableLiveData<String>()
    val notificaPrenotazione: LiveData<String> get() = _notificaPrenotazione

    private val _listaPrenotazioni = MutableLiveData<List<Prenotazione>>()
    val listaPrenotazioni: LiveData<List<Prenotazione>> get() = _listaPrenotazioni

    fun setPrenotazioni(prenotazioniSelezionate: List<Calendario>, studenteRef: String, annuncioRef: String, onComplete: () -> Unit) {
        listaCreaPrenotazioni = prenotazioniSelezionate

        // Chiamata a FirebaseUtil per salvare le prenotazioni su Firestore
        FirebaseUtil.creaPrenotazioniConBatch(
            listaFasceSelezionate = prenotazioniSelezionate,
            idStudente = studenteRef,
            annuncioId = annuncioRef,
            onSuccess = {
                caricaPrenotazioni(false, studenteRef)
                onComplete()
            },
            onFailure = {
            }
        )
    }

    // Funzione unica per caricare le prenotazioni in base al ruolo
    fun caricaPrenotazioni(ruolo: Boolean, userId: String) {
        FirebaseUtil.getPrenotazioniPerRuolo(
            userId = userId,
            isTutor = ruolo,
            onSuccess = { prenotazioniList ->
                _listaPrenotazioni.value = prenotazioniList
            },
            onFailure = {
            }
        )
    }

    fun eliminaPrenotazione(prenotazione: Prenotazione) {
        FirebaseUtil.eliminaPrenotazioneF(
            prenotazione,
            onSuccess = {
                // Verifica che la lista delle prenotazioni non sia null e aggiorna la lista
                val nuovaLista = _listaPrenotazioni.value?.toMutableList() ?: mutableListOf()

                // Rimuovi la prenotazione dalla lista se presente
                if (nuovaLista.remove(prenotazione)) {
                    _listaPrenotazioni.value = nuovaLista
                } else {
                    _notificaPrenotazione.postValue("Prenotazione non trovata nella lista, aggiorna la pagina")
                }
            },
            onFailure = {
            }
        )
    }

}

