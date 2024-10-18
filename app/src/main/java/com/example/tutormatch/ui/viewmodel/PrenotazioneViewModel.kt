package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.util.FirebaseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PrenotazioneViewModel : ViewModel() {

    // Cambia da LiveData a una semplice variabile privata non osservata
    private var listaCreaPrenotazioni: List<Calendario> = emptyList()

    // LiveData per annuncio, studente e tutor
    private val _datiChat = MutableLiveData<Triple<Annuncio?, Utente?, Utente?>>()
    val datiChat: LiveData<Triple<Annuncio?, Utente?, Utente?>> get() = _datiChat

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
        // Avvia una coroutine nel ViewModel
        viewModelScope.launch {
            try {
                // Sposta l'operazione di eliminazione su un thread di I/O
                val success = withContext(Dispatchers.IO) {
                    FirebaseUtil.eliminaPrenotazioneF(prenotazione)
                }

                if (success) {
                    // Verifica che la lista delle prenotazioni non sia null e aggiorna la lista
                    val nuovaLista = _listaPrenotazioni.value?.toMutableList() ?: mutableListOf()

                    // Rimuovi la prenotazione dalla lista se presente
                    if (nuovaLista.remove(prenotazione)) {
                        _listaPrenotazioni.value = nuovaLista
                    }
                } else {
                    // Notifica all'utente che la prenotazione non è stata trovata
                    _notificaPrenotazione.postValue("Prenotazione non trovata nella lista, aggiorna la pagina")
                }
            } catch (e: Exception) {
                // Gestisci eventuali errori durante l'eliminazione
                _notificaPrenotazione.postValue("Errore durante l'eliminazione della prenotazione: ${e.message}")
            }
        }
    }

    // Funzione per recuperare i dati necessari per la chat
    fun recuperaDatiChat(prenotazione: Prenotazione) {
        viewModelScope.launch {
            try {
                // Recupera l'annuncio
                val annuncio = FirebaseUtil.getAnnuncio(prenotazione.annuncioRef!!)
                val studente = FirebaseUtil.getUserFromFirestore(prenotazione.studenteRef)
                val tutor = annuncio?.tutor?.id?.let { tutorId ->
                    FirebaseUtil.getUserFromFirestore(tutorId)
                }

                // Aggiorna il LiveData con i dati
                _datiChat.postValue(Triple(annuncio, studente, tutor))
            } catch (e: Exception) {
                _datiChat.postValue(Triple(null, null, null))
            }
        }
    }



}

