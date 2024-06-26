package com.example.tutormatch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tutormatch.db.CentralDatabase
import com.example.tutormatch.db.Utente

class ProfiloViewModel(application: Application): AndroidViewModel(application) {

    private val db:CentralDatabase = CentralDatabase.getInstance(application) //instanzio il db

    private var _utente = MutableLiveData(Utente("","","","","","","",false))
    val utente: LiveData<Utente> get() = _utente

    private var _listaUtente = MutableLiveData(listOf<Utente>())
    val listaUtente: LiveData<List<Utente>> get() = _listaUtente

    fun leggiUtente(email: String){
        _utente.value = db.utenteDao().getUtenteByEmail(email)    }

    fun leggiTuttiUtenti(){
        val x = db.utenteDao().getAllUtenti()
        _listaUtente.value = x
    }

    fun insert(utente: Utente){
        db.utenteDao().insert(utente)
    }

    fun update(utente: Utente){
        db.utenteDao().update(utente)
    }

    fun delete(utente: Utente){
        db.utenteDao().delete(utente)
    }


    //fun insert(vararg student: Student){
    //db.studentDao().insert(*student) //* spaccetta un array in una lista separata di elementi
    //}
}