package com.example.tutormatch.ui.lista

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tutormatch.db.CentralDatabase
import com.example.tutormatch.db.Prenotazione

class ListaViewModel(application: Application): AndroidViewModel(application){

    private val db: CentralDatabase = CentralDatabase.getInstance(application) //instanzio il db

    private var _prenotazione = MutableLiveData(Prenotazione("","",0,0,null,null))
    val prenotazione: LiveData<Prenotazione> get() = _prenotazione

    private var _listaPrenotazione = MutableLiveData(listOf<Prenotazione>())
    val listaPrenotazione: LiveData<List<Prenotazione>> get() = _listaPrenotazione

   // fun leggiPrenotazione(email: String){
        //_prenotazione.value = db.prenotazioneDao().  }

}