//package com.example.tutormatch.ui.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.tutormatch.data.Prenotazione
//import kotlinx.coroutines.launch
//
//class PrenotazioniViewModel : ViewModel() {
//
//    private val _prenotazioni = MutableLiveData<List<Prenotazione>>()
//    val prenotazioni: LiveData<List<Prenotazione>> get() = _prenotazioni
//
//    init {
//        loadPrenotazioni()
//    }
//
//    private fun loadPrenotazioni() {
//        viewModelScope.launch {
//            // Carica le prenotazioni dal database o da una sorgente dati
//            _prenotazioni.value = getPrenotazioniFromDatabase()
//        }
//    }
//
//    private fun getPrenotazioniFromDatabase(): List<Prenotazione> {
//        // Implementa il recupero delle prenotazioni dal database
//        return listOf()
//    }
//
//    fun eliminaPrenotazione(prenotazione: Prenotazione) {
//        viewModelScope.launch {
//            // Elimina la prenotazione dal database
//            _prenotazioni.value = _prenotazioni.value?.filter { it != prenotazione }
//        }
//    }
//}
