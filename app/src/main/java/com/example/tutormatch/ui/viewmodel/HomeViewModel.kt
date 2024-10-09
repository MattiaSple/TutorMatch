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
        tutorDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val tutor = documentSnapshot.toObject(Utente::class.java)
                tutor?.let {
                    val updatedRatings = it.feedback.toMutableList()
                    updatedRatings.add(rating)
                    tutorDocRef.update("feedback", updatedRatings)
                }
            }
        }

        // Rimuovi il tutor dalla lista tutorRef dello studente
        val studentDocRef = db.collection("utenti").document(studentRef)
        studentDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val student = documentSnapshot.toObject(Utente::class.java)
                student?.let {
                    val updatedTutorRefs = it.tutorDaValutare.toMutableList()
                    updatedTutorRefs.remove(tutor.userId)
                    studentDocRef.update("tutorRef", updatedTutorRefs)
                    // Aggiorna il LiveData dei tutorRef
                    loadTutorRefs(updatedTutorRefs)
                }
            }
        }
    }
}
