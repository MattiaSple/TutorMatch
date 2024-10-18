package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.util.FirebaseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // LiveData che contiene la lista dei riferimenti ai tutor (tutorRef)
    private val _tutorRefs = MutableLiveData<List<String>>()
    val tutorRefs: LiveData<List<String>> get() = _tutorRefs

    // LiveData che contiene la lista dei tutor
    private val _tutors = MutableLiveData<List<Utente>>()
    val tutors: LiveData<List<Utente>> get() = _tutors

    // Funzione per caricare i riferimenti ai tutor dell'utente
    fun loadTutorRefs(tutorRefs: List<String>) {
        _tutorRefs.value = tutorRefs
        loadTutors() // Carica i tutor automaticamente quando i riferimenti cambiano
    }

    fun getListaTutorDaValutare(userId: String) {
        viewModelScope.launch {
            try {
                // Chiama FirebaseUtil per ottenere l'utente
                val utente = FirebaseUtil.getUserFromFirestore(userId)

                utente?.let {
                    val listaTutorRefs = it.tutorDaValutare
                    listaTutorRefs?.let { refs ->
                        // Aggiorna la LiveData con i riferimenti ai tutor
                        loadTutorRefs(refs)
                    }
                }
            } catch (e: Exception) {
                // Gestisci l'errore
                _message.postValue("Errore nel caricamento dei dati dell'utente")
            }
        }
    }

    // Funzione per caricare i tutor associati dalla lista tutorRefs
    fun loadTutors() {
        viewModelScope.launch {
            _tutorRefs.value?.let { tutorRefs ->
                try {
                    // Chiama FirebaseUtil per caricare i tutor in modo asincrono
                    val tutorList = FirebaseUtil.loadTutors(tutorRefs)
                    // Aggiorna il LiveData con la lista dei tutor
                    _tutors.postValue(tutorList)
                } catch (e: Exception) {
                    // Gestisci l'errore
                    _message.postValue("Errore nel caricamento dei tutor")
                }
            }
        }
    }


    fun rateTutorAndRemoveFromList(studentRef: String, tutor: Utente, rating: Int) {
        // Avvia una coroutine per chiamare la funzione suspend
        viewModelScope.launch {
            // Sposta l'operazione di I/O su Dispatchers.IO
            val success = withContext(Dispatchers.IO) {
                FirebaseUtil.rateTutorAndRemoveFromListBatch(studentRef, tutor.userId, rating)
            }

            // Dopo aver completato l'operazione di I/O, gestisci i risultati nel Main Thread
            if (success) {
                // Aggiorna la lista dei tutor rimuovendo il tutor valutato
                val updatedTutorList = _tutors.value?.toMutableList() ?: mutableListOf()
                updatedTutorList.remove(tutor)
                _tutors.value = updatedTutorList

                // Aggiorna i riferimenti ai tutor
                val updatedTutorRefs = _tutorRefs.value?.toMutableList() ?: mutableListOf()
                updatedTutorRefs.remove(tutor.userId)
                _tutorRefs.value = updatedTutorRefs
                _message.value = "Feedback inviato!"

            } else {
                _message.value = "Feedback non andato a buon fine"
            }
        }
    }

}
