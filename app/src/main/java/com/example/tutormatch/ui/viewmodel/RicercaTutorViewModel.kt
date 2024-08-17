package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tutormatch.data.model.Annuncio
import com.google.firebase.firestore.FirebaseFirestore

class RicercaTutorViewModel(application: Application) : AndroidViewModel(application) {

    private val _annunci = MutableLiveData<List<Annuncio>>()
    val annunci: LiveData<List<Annuncio>> get() = _annunci

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Cache in memoria per gli annunci
    private var cachedAnnunci: List<Annuncio>? = null

    // Funzione per caricare gli annunci (con cache)
    fun loadAnnunci(forceRefresh: Boolean = false) {
        if (cachedAnnunci != null && !forceRefresh) {
            // Usa i dati dalla cache se esistono e non è richiesto un aggiornamento
            _annunci.value = cachedAnnunci!!
        } else {
            // Altrimenti carica gli annunci da Firestore
            fetchAnnunciFromFirestore()
        }
    }

    // Funzione per ottenere gli annunci da Firestore
    private fun fetchAnnunciFromFirestore() {
        db.collection("annunci")
            .get()
            .addOnSuccessListener { documents ->
                val annuncioList = mutableListOf<Annuncio>()
                for (document in documents) {
                    val annuncio = document.toObject(Annuncio::class.java)
                    annuncioList.add(annuncio)
                }
                cachedAnnunci = annuncioList
                _annunci.value = annuncioList
            }
            .addOnFailureListener {
                // Gestisci l'errore
            }
    }

    // Metodo per verificare la presenza di nuovi annunci
    fun checkForNewAnnunci() {
        db.collection("annunci")
            .get()
            .addOnSuccessListener { documents ->
                val annuncioList = mutableListOf<Annuncio>()
                for (document in documents) {
                    val annuncio = document.toObject(Annuncio::class.java)
                    annuncioList.add(annuncio)
                }

                // Controlla se ci sono nuovi annunci rispetto alla cache
                if (annuncioList != cachedAnnunci) {
                    cachedAnnunci = annuncioList
                    _annunci.value = annuncioList
                }
            }
            .addOnFailureListener {
                // Gestisci l'errore
            }
    }

    // Funzione per applicare i filtri
    fun applyFilters(materia: String, budget: Double, modalita: String) {
        db.collection("annunci")
            .whereEqualTo("materia", materia)
            .whereLessThanOrEqualTo("prezzo", budget.toString())
            .get()
            .addOnSuccessListener { documents ->
                val annuncioList = mutableListOf<Annuncio>()
                for (document in documents) {
                    val annuncio = document.toObject(Annuncio::class.java)
                    if ((modalita == "Online" && annuncio.mod_on) || (modalita == "In presenza" && annuncio.mod_pres)) {
                        annuncioList.add(annuncio)
                    }
                }
                _annunci.value = annuncioList
            }
            .addOnFailureListener {
                // Gestisci l'errore
            }
    }
}

