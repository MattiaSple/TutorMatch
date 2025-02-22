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

    // LiveData per annuncio, studente e tutor
    private val _datiChat = MutableLiveData<Triple<Annuncio?, Utente?, Utente?>>()
    val datiChat: LiveData<Triple<Annuncio?, Utente?, Utente?>> get() = _datiChat

    // LiveData per gestire i messaggi di notifica
    private val _notificaPrenotazione = MutableLiveData<String>()
    val notificaPrenotazione: LiveData<String> get() = _notificaPrenotazione

    private val _listaPrenotazioni = MutableLiveData<List<Prenotazione>>()
    val listaPrenotazioni: LiveData<List<Prenotazione>> get() = _listaPrenotazioni

    private val _prenotazioneSuccesso = MutableLiveData<Boolean>()
    val prenotazioneSuccesso: LiveData<Boolean> get() = _prenotazioneSuccesso

    fun creaPrenotazioni(prenotazioniSelezionate: List<Calendario>, studenteRef: String, annuncioRef: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Chiamata a FirebaseUtil per salvare le prenotazioni su Firestore
                val risultato = FirebaseUtil.creaPrenotazioniConBatch(
                    listaFasceSelezionate = prenotazioniSelezionate,
                    idStudente = studenteRef,
                    annuncioId = annuncioRef
                )

                if (risultato) {
                    _prenotazioneSuccesso.postValue(true)  // Successo
                } else {
                    _prenotazioneSuccesso.postValue(false)  // Fallimento
                }
            } catch (e: Exception) {
                _prenotazioneSuccesso.postValue(false)  // Fallimento
            }
        }
    }

    // Funzione unica per caricare le prenotazioni in base al ruolo
    fun caricaPrenotazioni(ruolo: Boolean, userId: String) {
        viewModelScope.launch {
            try {
                // Chiamata a FirebaseUtil per ottenere la lista di prenotazioni ordinate
                val prenotazioniList = FirebaseUtil.getPrenotazioniPerRuolo(userId, ruolo)
                _listaPrenotazioni.postValue(prenotazioniList)
            } catch (e: Exception) {
                // Gestione dell'errore, ad esempio mostrando un messaggio
                _notificaPrenotazione.postValue("Errore durante il caricamento delle prenotazioni.")
                _listaPrenotazioni.postValue(emptyList())
            }
        }
    }



    fun eliminaPrenotazione(prenotazione: Prenotazione) {
        // Avvia una coroutine nel ViewModel
        viewModelScope.launch {
            try {
                // Sposta l'operazione di eliminazione su un thread di I/O
                val success = withContext(Dispatchers.IO) {
                    FirebaseUtil.eliminaPrenotazioneF(prenotazione)
                }
                // Verifica che la lista delle prenotazioni non sia null
                val nuovaLista = _listaPrenotazioni.value?.toMutableList() ?: mutableListOf()
                if (success) {
                    // Rimuovi la prenotazione dalla lista se presente
                    if (nuovaLista.remove(prenotazione)) {
                        _listaPrenotazioni.value = nuovaLista
                    }
                } else {
                    // Notifica all'utente che la prenotazione non è stata trovata
                    _notificaPrenotazione.postValue("Prenotazione già eliminata")
                    nuovaLista.remove(prenotazione)
                    _listaPrenotazioni.value = nuovaLista
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
                val tutor = annuncio!!.tutor!!.id.let { tutorId ->
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

