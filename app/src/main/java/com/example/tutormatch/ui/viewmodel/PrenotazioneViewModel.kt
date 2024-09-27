package com.example.tutormatch.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.util.FirebaseUtil


class PrenotazioneViewModel : ViewModel() {

    private val _prenotazioni = MutableLiveData<List<Calendario>>()
    val prenotazioni: LiveData<List<Calendario>> get() = _prenotazioni

    fun setPrenotazioni(prenotazioniSelezionate: List<Calendario>, studenteRef: String, annuncioRef: String) {
        _prenotazioni.value = prenotazioniSelezionate

        // Chiamata a FirebaseUtil per salvare le prenotazioni su Firestore
        FirebaseUtil.creaPrenotazioniConBatch(
            listaFasceSelezionate = prenotazioniSelezionate,
            idStudente = studenteRef,
            annuncioId = annuncioRef,  // Aggiungi questo parametro
            onSuccess = {
                // Gestisci il successo, es: notificare l'utente
                Log.d("Prenotazione", "Prenotazioni salvate con successo")
            },
            onFailure = { exception ->
                // Gestisci il fallimento, es: mostra un messaggio di errore
                Log.e("Prenotazione", "Errore nel salvataggio delle prenotazioni: ${exception.message}")
            }
        )
    }

}
