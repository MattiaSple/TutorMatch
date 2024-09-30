package com.example.tutormatch.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.util.FirebaseUtil


class PrenotazioneViewModel : ViewModel() {

    // Cambia da LiveData a una semplice variabile privata non osservata
    private var listaCreaPrenotazioni: List<Calendario> = emptyList()

    private val _listaPrenotazioni = MutableLiveData<List<Prenotazione>>()
    val listaPrenotazioni: LiveData<List<Prenotazione>> get() = _listaPrenotazioni

    fun setPrenotazioni(prenotazioniSelezionate: List<Calendario>, studenteRef: String, annuncioRef: String) {
        listaCreaPrenotazioni = prenotazioniSelezionate

        // Chiamata a FirebaseUtil per salvare le prenotazioni su Firestore
        FirebaseUtil.creaPrenotazioniConBatch(
            listaFasceSelezionate = prenotazioniSelezionate,
            idStudente = studenteRef,
            annuncioId = annuncioRef,
            onSuccess = {
                Log.d("Prenotazione", "Prenotazioni salvate con successo")
            },
            onFailure = { exception ->
                Log.e("Prenotazione", "Errore nel salvataggio delle prenotazioni: ${exception.message}")
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
            onFailure = { exception ->
                Log.e("PrenotazioneViewModel", "Errore nel caricamento delle prenotazioni: ${exception.message}")
            }
        )
    }
}

