package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.source.CentralDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnnunciViewModel(application: Application) : AndroidViewModel(application) {

    // DAO per accedere al database
    private val annuncioDao = CentralDatabase.getInstance(application).annuncioDao()

    // LiveData per gli annunci
    private val _annunci = MutableLiveData<List<Annuncio>>()
    val annunci: LiveData<List<Annuncio>> get() = _annunci

    // LiveData per i campi di input
    val materia = MutableLiveData<String>()
    val costo = MutableLiveData<String>()
    val descrizione = MutableLiveData<String>()
    val online = MutableLiveData<Boolean>()
    val presenza = MutableLiveData<Boolean>()

    private var _emailTutor: String? = null

    init {
        loadAnnunci()
    }

    // Funzione per impostare l'email del tutor
    fun setEmailTutor(email: String) {
        _emailTutor = email
        loadAnnunci() // Carica gli annunci una volta che l'email è stata impostata
    }

    // Funzione per caricare gli annunci dal database
    private fun loadAnnunci() {
        _emailTutor?.let { email ->
            viewModelScope.launch(Dispatchers.IO) {
                val loadedAnnunci = annuncioDao.getAnnunciByEmail(email)
                _annunci.postValue(loadedAnnunci)
            }
        }
    }

    // Funzione per salvare un nuovo annuncio
    fun salvaAnnuncio(): Boolean {
        val materiaVal = materia.value ?: ""
        val costoVal = costo.value ?: ""
        val descrizioneVal = descrizione.value ?: ""

        // Verifica che tutti i campi siano compilati
        if (materiaVal.isBlank() || costoVal.isBlank() || descrizioneVal.isBlank()) {
            return false
        }

        // Crea un nuovo oggetto Annuncio
        val nuovoAnnuncio = Annuncio(
            email_prof = _emailTutor ?: return false,
            materia = materiaVal,
            prezzo = costoVal.toInt(),
            descrizione = descrizioneVal,
            mod_ins = online.value == true
        )

        // Inserisce l'annuncio nel database
        viewModelScope.launch(Dispatchers.IO) {
            annuncioDao.insert(nuovoAnnuncio)
            loadAnnunci()  // Aggiorna la lista degli annunci
        }
        return true
    }

    // Funzione per eliminare un annuncio
    fun eliminaAnnuncio(annuncio: Annuncio) {
        viewModelScope.launch(Dispatchers.IO) {
            annuncioDao.delete(annuncio)
            loadAnnunci()  // Aggiorna la lista degli annunci
        }
    }
}
