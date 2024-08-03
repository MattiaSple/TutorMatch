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
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

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
                    email.postValue(it.email)
                    ruolo = it.ruolo // Preserva il ruolo
                }
            } catch (e: Exception) {
                // Gestisci l'errore
            }
        }
    }

    // Salva i dati del profilo utente
    fun saveUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedUser = Utente(
                    userId = userId,
                    nome = nome.value ?: "",
                    cognome = cognome.value ?: "",
                    email = email.value ?: "",
                    ruolo = ruolo // Usa il ruolo preservato
                )
                utentiCollection.document(userId).set(updatedUser).await()
                // Puoi aggiungere un messaggio di successo se necessario
            } catch (e: Exception) {
                // Gestisci l'errore
            }
        }
    }
}
