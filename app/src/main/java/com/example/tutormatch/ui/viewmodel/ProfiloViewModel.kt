package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Utente
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfiloViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val utentiCollection = firestore.collection("utenti")

    val nome = MutableLiveData<String>()
    val cognome = MutableLiveData<String>()
    val residenza = MutableLiveData<String>()
    val via = MutableLiveData<String>()
    val civico = MutableLiveData<String>()
    val message = MutableLiveData<String>()

    private var ruolo: Boolean = false // Variabile per preservare il ruolo

    // Carica i dati del profilo utente
    fun loadUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = utentiCollection.document(userId).get().await()
                val utente = documentSnapshot.toObject(Utente::class.java)
                utente?.let {
                    nome.postValue(it.nome)
                    cognome.postValue(it.cognome)
                    residenza.postValue(it.residenza)
                    via.postValue(it.via)
                    civico.postValue(it.civico)
                    ruolo = it.ruolo // Preserva il ruolo
                }
            } catch (e: Exception) {
                message.postValue("Errore nel caricamento del profilo utente.")
            }
        }
    }

    // Controlla che tutti i campi non siano vuoti
    private fun areFieldsValid(): Boolean {
        return nome.value!!.isNotEmpty() &&
                cognome.value!!.isNotEmpty() &&
                residenza.value!!.isNotEmpty() &&
                via.value!!.isNotEmpty() &&
                civico.value!!.isNotEmpty()
    }

    // Salva i dati del profilo utente
    fun saveUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (areFieldsValid()) {
                    val updatedUser = Utente(
                        userId = userId,
                        nome = nome.value ?: "",
                        cognome = cognome.value ?: "",
                        residenza = residenza.value ?: "",
                        via = via.value ?: "",
                        civico = civico.value ?: "",
                        ruolo = ruolo // Usa il ruolo preservato
                    )
                    utentiCollection.document(userId).set(updatedUser).await()
                    message.postValue("Profilo salvato con successo.")
                } else {
                    message.postValue("Tutti i campi devono essere compilati.")
                }
            } catch (e: Exception) {
                message.postValue("Errore nel salvataggio del profilo utente.")
            }
        }
    }
}
