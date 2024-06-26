package com.example.tutormatch.ui.lista

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tutormatch.db.CentralDatabase
import com.example.tutormatch.db.Prenotazione

class ListaViewModel(application: Application): AndroidViewModel(application){

    private val db: CentralDatabase = CentralDatabase.getInstance(application) //instanzio il db

    private var _prenotazione = MutableLiveData(Prenotazione("","",0,0))
    val prenotazione: LiveData<Prenotazione> get() = _prenotazione

    private var _listaPrenotazione = MutableLiveData(listOf<Prenotazione>())
    val listaPrenotazione: LiveData<List<Prenotazione>> get() = _listaPrenotazione

//    fun leggiPrenotazione(email: String, id_calendario: Int){
//        _prenotazione.value = db.prenotazioneDao().getInfoPrenotazione(email, id_calendario)}

    fun eliminaPrenotazione(prenotazione: Prenotazione){
        db.prenotazioneDao().delete(prenotazione)
    }

    fun listaPrenotazioni(email: String){
        val x = db.prenotazioneDao().getAllPrenotazioniByEmail(email)
        _listaPrenotazione.value = x
    }

    fun insert(prenotazione: Prenotazione){
        db.prenotazioneDao().insert(prenotazione)
    }

}