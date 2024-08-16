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

    fun loadAnnunci() {
        db.collection("annunci")
            .get()
            .addOnSuccessListener { documents ->
                val annuncioList = mutableListOf<Annuncio>()
                for (document in documents) {
                    val annuncio = document.toObject(Annuncio::class.java)
                    annuncioList.add(annuncio)
                }
                _annunci.value = annuncioList
            }
            .addOnFailureListener {
                // Gestisci l'errore
            }
    }

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
