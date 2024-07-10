package com.example.tutormatch.ui.lista

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import com.example.tutormatch.db.CentralDatabase
import com.example.tutormatch.db.Utente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UtenteViewModel(application: Application) : AndroidViewModel(application) {

    private val db: CentralDatabase = CentralDatabase.getInstance(application)
    private val utenteDao = db.utenteDao()

    private val _utente = MutableLiveData<Utente?>() //varaibile privata modificabile
    val utente: LiveData<Utente?> //le classi esterne accederanno solo a questa in lettura
        get() = _utente

    private val _listaUtenti = MutableLiveData<List<Utente>>().apply { value = emptyList() }
    val listaUtenti: LiveData<List<Utente>> get() = _listaUtenti

    fun getUtenteByEmail(email: String) {
        viewModelScope.launch {
            val utente = withContext(Dispatchers.IO) {
                utenteDao.getUtenteByEmail(email)
            }
            _utente.postValue(utente)
        }
    }

    fun getAllUtenti() {
        viewModelScope.launch {
            val utenti = withContext(Dispatchers.IO) {
                utenteDao.getAllUtenti()
            }
            _listaUtenti.postValue(utenti ?: emptyList())
        }
    }

    fun insert(utente: Utente) {
        viewModelScope.launch(Dispatchers.IO) {
            utenteDao.insert(utente)
            val aggiornataLista = utenteDao.getAllUtenti()
            withContext(Dispatchers.Main) {
                _listaUtenti.postValue(aggiornataLista)
                Toast.makeText(getApplication(), "Account creato!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun update(utente: Utente) {
        viewModelScope.launch(Dispatchers.IO) {
            utenteDao.update(utente)
            val aggiornataLista = utenteDao.getAllUtenti()
            withContext(Dispatchers.Main) {
                _listaUtenti.postValue(aggiornataLista)
            }
        }
    }

    fun delete(utente: Utente) {
        viewModelScope.launch(Dispatchers.IO) {
            utenteDao.delete(utente)
            val aggiornataLista = utenteDao.getAllUtenti()
            withContext(Dispatchers.Main) {
                _listaUtenti.postValue(aggiornataLista)
            }
        }
    }
}
