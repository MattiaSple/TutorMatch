package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.network.RetrofitInstance
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
    val cap = MutableLiveData<String>()
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
                    cap.postValue(it.cap)
                    ruolo = it.ruolo // Preserva il ruolo
                }
            } catch (e: Exception) {
                message.postValue("Errore nel caricamento del profilo utente.")
            }
        }
    }

    // Controlla che tutti i campi non siano vuoti
    private fun areFieldsValid(): Boolean {
        return !nome.value.isNullOrEmpty() &&
                !cognome.value.isNullOrEmpty() &&
                !residenza.value.isNullOrEmpty() &&
                !via.value.isNullOrEmpty() &&
                !cap.value.isNullOrEmpty()
    }

    fun saveUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (areFieldsValid()) {
                    val updates = mutableMapOf<String, Any>()

                    // Aggiungi i campi che desideri aggiornare
                    nome.value?.let { updates["nome"] = it }
                    cognome.value?.let { updates["cognome"] = it }
                    via.value?.let { updates["via"] = it }

                    // Prima di modificare l'utente, controlla la validità dell'indirizzo

                    val indirizzoSenzaVia = "${cap.value}, ${residenza.value}"
                    if(verificaIndirizzo(indirizzoSenzaVia))
                    {
                        residenza.value?.let { updates["residenza"] = it }
                        cap.value?.let { updates["cap"] = it }
                    }else{
                        // Se l'indirizzo non è valido, invia un messaggio di errore
                        message.postValue("Residenza o CAP non validi.")
                        return@launch
                    }


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

                    if(ruolo)
                    {
                        // Elimina tutti gli annunci associati all'utente
                        val annunci = db.collection("annunci")
                            .whereEqualTo("tutor", db.document("utenti/$userId")).get().await()
                        for (annuncio in annunci.documents) {
                            batch.delete(annuncio.reference)
                        }

                        // Elimina tutte le prenotazioni associate all'utente
                        val prenotazioni = db.collection("prenotazioni")
                            .whereEqualTo("tutor", db.document("utenti/$userId")).get().await()
                        for (prenotazione in prenotazioni.documents) {
                            batch.delete(prenotazione.reference)
                        }

                        // Elimina tutte le date del calendario associate all'utente
                        val calendario = db.collection("calendario")
                            .whereEqualTo("tutorRef", db.document("utenti/$userId")).get().await()
                        for (fascia in calendario.documents) {
                            batch.delete(fascia.reference)
                        }
                    } else{
                        message.postValue("ANCORA DA IMPLEMENTARE")
                    }

                    // Commit del batch
                    batch.commit().await()

                    // Elimina l'account utente dalla Firebase Authentication
                    user.delete().await()

                    message.postValue("Account e dati associati eliminati con successo.")

                } catch (e: Exception) {

                    message.postValue("Errore durante l'eliminazione dell'account: ${e.message}")
                }
            }
        }
    }

    private suspend fun verificaIndirizzo(indirizzo: String): Boolean {
        return withContext(Dispatchers.IO) {
            val call = RetrofitInstance.api.getLocation(indirizzo)
            try {
                val response = call.execute()
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val location = response.body()!![0]

                    // Ottieni il CAP e la città dal display_name o dall'address (se disponibile)
                    val displayName = location.display_name.lowercase()

                    // Converti le stringhe in minuscolo per fare il confronto
                    val capValido = displayName.contains(cap.value!!)
                    val residenzaValida = displayName.contains(residenza.value!!.lowercase())

                    // Se il CAP e la città corrispondono, restituisci true
                    if (capValido && residenzaValida) {
                        return@withContext true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext false
        }
    }
}