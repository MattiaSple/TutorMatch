package com.example.tutormatch.ui.lista

import android.app.Application
import androidx.lifecycle.*
import com.example.tutormatch.data.source.CentralDatabase
import com.example.tutormatch.data.model.Prenotazione
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrenotazioneViewModel(application: Application) : AndroidViewModel(application) {

    private val db: CentralDatabase = CentralDatabase.getInstance(application)
    private val prenotazioneDao = db.prenotazioneDao()

    private val _prenotazione = MutableLiveData<Prenotazione>()
    val prenotazione: LiveData<Prenotazione> get() = _prenotazione

    private val _listaPrenotazione = MutableLiveData<List<Prenotazione>>()
    val listaPrenotazione: LiveData<List<Prenotazione>> get() = _listaPrenotazione

//    fun leggiPrenotazione(email: String, idCalendario: Int) {
//        viewModelScope.launch {
//            val prenotazione = withContext(Dispatchers.IO) {
//                prenotazioneDao.getInfoPrenotazione(email, idCalendario)
//            }
//            _prenotazione.value = prenotazione
//        }
//    }

    fun eliminaPrenotazione(prenotazione: Prenotazione) {
        viewModelScope.launch(Dispatchers.IO) {
            prenotazioneDao.delete(prenotazione)
            val aggiornataLista = prenotazioneDao.getAllPrenotazioniByEmail(prenotazione.email_studente)
            withContext(Dispatchers.Main) {
                _listaPrenotazione.value = aggiornataLista
            }
        }
    }

    fun listaPrenotazioni(email: String) {
        viewModelScope.launch {
            val prenotazioni = withContext(Dispatchers.IO) {
                prenotazioneDao.getAllPrenotazioniByEmail(email)
            }
            _listaPrenotazione.value = prenotazioni
        }
    }

    fun insert(prenotazione: Prenotazione) {
        viewModelScope.launch(Dispatchers.IO) {
            prenotazioneDao.insert(prenotazione)
            val aggiornataLista = prenotazioneDao.getAllPrenotazioniByEmail(prenotazione.email_studente)
            withContext(Dispatchers.Main) {
                _listaPrenotazione.value = aggiornataLista
            }
        }
    }
}
