package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Prenotazione
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch

class PrenotazioniViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val prenotazioniCollection = db.collection("prenotazioni")

    private val _prenotazioni = MutableLiveData<List<Prenotazione>>()
    val prenotazioni: LiveData<List<Prenotazione>> get() = _prenotazioni

    init {
        loadPrenotazioni()
    }

    private fun loadPrenotazioni() {
        prenotazioniCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                _prenotazioni.value = emptyList()
                return@addSnapshotListener
            }

            val prenotazioniList = snapshot?.documents?.mapNotNull { it.toObject<Prenotazione>() }
            _prenotazioni.value = prenotazioniList ?: emptyList()
        }
    }
}
