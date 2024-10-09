package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Utente
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

    // Funzione per caricare i tutor associati dalla lista tutorRef
    fun loadTutors() {
        val tutorList = mutableListOf<Utente>()

        _tutorRefs.value?.forEach { tutorRef ->
            db.collection("utenti").document(tutorRef).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val tutor = documentSnapshot.toObject(Utente::class.java)
                        tutor?.let { tutorList.add(it) }
                    }
                    _tutors.value = tutorList
                }
                .addOnFailureListener { e ->
                    // Gestione degli errori
                }
        }
    }

    // Funzione per valutare il tutor e rimuoverlo dalla lista
    fun rateTutorAndRemoveFromList(studentRef: String, tutor: Utente, rating: Int) {
        val tutorDocRef = db.collection("utenti").document(tutor.userId)
        val studentDocRef = db.collection("utenti").document(studentRef)

        // Aggiorna la valutazione del tutor
        tutorDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val tutor = documentSnapshot.toObject(Utente::class.java)
                tutor?.let {
                    val updatedRatings = it.feedback.toMutableList().apply {
                        add(rating)
                    }
                    tutorDocRef.update("feedback", updatedRatings)
                }
            }
        }

        // Rimuovi il tutor dalla lista tutorDaValutare dello studente
        studentDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val student = documentSnapshot.toObject(Utente::class.java)
                student?.let {
                    val updatedTutorRefs = it.tutorDaValutare.toMutableList()
                    if (updatedTutorRefs.contains(tutor.userId)) {
                        updatedTutorRefs.remove(tutor.userId)
                        studentDocRef.update("tutorDaValutare", updatedTutorRefs).addOnSuccessListener {
                            // Aggiorna i riferimenti ai tutor e la lista dei tutor
                            loadTutorRefs(updatedTutorRefs)

                            // Aggiorna il LiveData dei tutor rimuovendo il tutor valutato
                            val updatedTutorList = _tutors.value?.toMutableList() ?: mutableListOf()
                            updatedTutorList.remove(tutor)
                            _tutors.value = updatedTutorList
                        }.addOnFailureListener {
                            // Gestione degli errori in caso di aggiornamento fallito

                        }
                    }
                }
            }
        }
    }
}
