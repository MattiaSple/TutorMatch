package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _saluto = MutableLiveData<String>()
    val saluto: LiveData<String> get() = _saluto

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
        // Chiama FirebaseUtil per ottenere l'utente da Firestore
        FirebaseUtil.getUserFromFirestore(userId) { utente ->
            val listaTutorRefs = utente?.tutorDaValutare
            listaTutorRefs?.let {
                // Aggiorna la LiveData con i riferimenti ai tutor
                loadTutorRefs(listaTutorRefs)
            }
        }
    }
    // Funzione per caricare i tutor associati dalla lista tutorRefs
    fun loadTutors() {
        _tutorRefs.value?.let { tutorRefs ->
            FirebaseUtil.loadTutors(tutorRefs) { tutorList ->
                _tutors.value = tutorList
            }
        }
    }

    // Funzione per valutare il tutor e rimuoverlo dalla lista
    fun rateTutorAndRemoveFromList(studentRef: String, tutor: Utente, rating: Int) {
        FirebaseUtil.rateTutorAndRemoveFromList(studentRef, tutor.userId, rating) { success ->
            if (success) {
                // Aggiorna la lista dei tutor rimuovendo il tutor valutato
                val updatedTutorList = _tutors.value?.toMutableList() ?: mutableListOf()
                updatedTutorList.remove(tutor)
                _tutors.value = updatedTutorList

                // Aggiorna i riferimenti ai tutor
                val updatedTutorRefs = _tutorRefs.value?.toMutableList() ?: mutableListOf()
                updatedTutorRefs.remove(tutor.userId)
                _tutorRefs.value = updatedTutorRefs
            }
        }
    }
}
