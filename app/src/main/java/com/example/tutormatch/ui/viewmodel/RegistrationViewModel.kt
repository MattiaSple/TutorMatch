package com.example.tutormatch.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.tutormatch.data.source.CentralDatabase
import com.example.tutormatch.data.model.Utente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    //Istanzio UtenteDao per chiamate al db
    private val db: CentralDatabase = CentralDatabase.getInstance(application)
    private val utenteDao = db.utenteDao()

    //Live data e MutableData
    val nome = MutableLiveData<String>()
    val cognome = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val residenza = MutableLiveData<String>()
    val via = MutableLiveData<String>()
    val civico = MutableLiveData<String>()

    private var _ruolo: Boolean = false

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean>
        get() = _navigateBack

    private val _showMessage = MutableLiveData<String>()
    val showMessage: LiveData<String>
        get() = _showMessage

    private val _utente = MutableLiveData<Utente?>() //varaibile privata modificabile
    val utente: LiveData<Utente?> //le classi esterne accederanno solo a questa in lettura
        get() = _utente

//    private val _listaUtenti = MutableLiveData<List<Utente>>().apply { value = emptyList() }
//    val listaUtenti: LiveData<List<Utente>> get() = _listaUtenti


    fun setRuolo(ruolo_utente: Boolean){
        _ruolo = ruolo_utente
    }


    fun onRegisterClick() {
        if (!email.value.isNullOrEmpty() && !nome.value.isNullOrEmpty() && !cognome.value.isNullOrEmpty() &&
            !password.value.isNullOrEmpty() && !residenza.value.isNullOrEmpty() && !civico.value.isNullOrEmpty() &&
            !via.value.isNullOrEmpty() && email.value!!.contains("@") && email.value!!.contains(".")
        ) {
            val utente = Utente(
                email.value!!,
                nome.value!!,
                cognome.value!!,
                password.value!!,
                residenza.value!!,
                civico.value!!,
                via.value!!,
                _ruolo
            )

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    Log.d("RegistrationViewModel", "Attempting to insert user: $utente")
                    utenteDao.insert(utente)
                    _showMessage.postValue("Account creato!")
                    _navigateBack.postValue(true)
                    Log.d("RegistrationViewModel", "User inserted successfully: $utente")
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _showMessage.value = "Errore nella creazione dell'account: ${e.message}"
                    }
                }
            }
        } else {
            _showMessage.value = "Compila tutti i campi correttamente"
        }
    }

    fun onBackClick() {
        _navigateBack.value = true
    }

    fun onNavigatedBack() {
        _navigateBack.value = false
    }

    // Funzione per gestire il login
    fun onLoginClick() {
        if (!email.value.isNullOrEmpty() && !password.value.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val utente = utenteDao.getUtenteByEmail(email.value!!)
                if (utente != null && utente.password == password.value) {
                    _utente.postValue(utente)
                    _showMessage.postValue("Login riuscito!")
                } else {
                    _showMessage.postValue("Email o password errati!")
                }
            }
        } else {
            _showMessage.value = "Compila tutti i campi"
        }
    }

    fun insert(utente: Utente) {
        viewModelScope.launch(Dispatchers.IO) {
            utenteDao.insert(utente)
        }
    }


//    fun getUtenteByEmail(email: String) {
//        viewModelScope.launch {
//            val utente = withContext(Dispatchers.IO) {
//                utenteDao.getUtenteByEmail(email)
//            }
//            _utente.postValue(utente)
//        }
//    }
//
//    fun getAllUtenti() {
//        viewModelScope.launch {
//            val utenti = withContext(Dispatchers.IO) {
//                utenteDao.getAllUtenti()
//            }
//            _listaUtenti.postValue(utenti ?: emptyList())
//        }
//    }


//    fun update(utente: Utente) {
//        viewModelScope.launch(Dispatchers.IO) {
//            utenteDao.update(utente)
//            val aggiornataLista = utenteDao.getAllUtenti()
//            withContext(Dispatchers.Main) {
//                _listaUtenti.postValue(aggiornataLista)
//            }
//        }
//    }
//
//    fun delete(utente: Utente) {
//        viewModelScope.launch(Dispatchers.IO) {
//            utenteDao.delete(utente)
//            val aggiornataLista = utenteDao.getAllUtenti()
//            withContext(Dispatchers.Main) {
//                _listaUtenti.postValue(aggiornataLista)
//            }
//        }
    //}
}
