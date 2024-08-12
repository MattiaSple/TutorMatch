package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Utente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

    fun saveUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (areFieldsValid()) {
                    val updates = mutableMapOf<String, Any>()

                    // Aggiungi i campi che desideri aggiornare
                    nome.value?.let { updates["nome"] = it }
                    cognome.value?.let { updates["cognome"] = it }
                    residenza.value?.let { updates["residenza"] = it }
                    via.value?.let { updates["via"] = it }
                    civico.value?.let { updates["civico"] = it }

                    // Esegui l'aggiornamento del documento
                    val utentiCollection = FirebaseFirestore.getInstance().collection("utenti")
                    utentiCollection.document(userId).update(updates).await()
                    message.postValue("Profilo salvato con successo.")
                } else {
                    message.postValue("Tutti i campi devono essere compilati.")
                }
            } catch (e: Exception) {
                message.postValue("Errore nel salvataggio del profilo utente: ${e.message}")
            }
        }
    }


    fun eliminaDatiUtenteDaFirestore(userId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Elimina i dati dell'utente dal database Firestore
                    val db = FirebaseFirestore.getInstance()
                    val batch = db.batch()

                    // Elimina il documento principale dell'utente
                    val userDocRef = db.collection("utenti").document(userId)
                    batch.delete(userDocRef)

                    if (ruolo) {

                        // Elimina tutti gli annunci associati all'utente
                        val annunci = db.collection("annunci")
                            .whereEqualTo("tutor", db.document("utenti/$userId")).get().await()
                        for (annuncio in annunci.documents) {
                            batch.delete(annuncio.reference)
                        }

                        // Elimina tutte le prenotazioni come tutor
                        val prenotazioniTutor = db.collection("prenotazioni")
                            .whereEqualTo("tutor", db.document("utenti/$userId")).get().await()
                        for (prenotazione in prenotazioniTutor.documents) {
                            batch.delete(prenotazione.reference)
                        }

                        // Elimina tutte le date del calendario associate all'utente
                        val calendario = db.collection("calendario")
                            .whereEqualTo("tutorRef", db.document("utenti/$userId")).get().await()
                        for (fascia in calendario.documents) {
                            batch.delete(fascia.reference)
                        }
                    } else {
                        // Se l'utente è uno studente
                        // Elimina tutte le prenotazioni come studente
                        val prenotazioniStudente = db.collection("prenotazioni")
                            .whereEqualTo("studente", db.document("utenti/$userId")).get().await()
                        for (prenotazione in prenotazioniStudente.documents) {
                            batch.delete(prenotazione.reference)
                        }
                    }

                    // Commit del batch
                    batch.commit().await()

                    // Elimina l'account utente dalla Firebase Authentication
                    user.delete().await()

                    withContext(Dispatchers.Main) {
                        message.postValue("Account e dati associati eliminati con successo.")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        message.postValue("Errore durante l'eliminazione dell'account: ${e.message}")
                    }
                }
            }
        }
    }
}
