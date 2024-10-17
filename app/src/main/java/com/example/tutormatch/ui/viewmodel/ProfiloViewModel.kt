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
    var utentiCollection = firestore.collection("utenti")

    val nome = MutableLiveData<String>()
    val cognome = MutableLiveData<String>()
    val residenza = MutableLiveData<String>()
    val via = MutableLiveData<String>()
    val cap = MutableLiveData<String>()
    val message = MutableLiveData<String>()

    val mediaValutazioni = MutableLiveData<String>()
    val isTutor = MutableLiveData<Boolean>()
    val addressVerified = MutableLiveData<Boolean>() // Nuovo LiveData per la verifica dell'indirizzo

    private var ruolo: Boolean = false

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
                    ruolo = it.ruolo

                    // Aggiorna la variabile isTutor
                    isTutor.postValue(it.ruolo)

                    if (ruolo && it.feedback.isNotEmpty()) {
                        // Calcola la media delle valutazioni
                        val media = it.feedback.average()
                        mediaValutazioni.postValue("Valutazioni: %.1f".format(media))
                    }
                    else
                    {
                        mediaValutazioni.postValue("Valutazione: 0")
                    }
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
                    val isAddressValid = verificaIndirizzo(indirizzoSenzaVia)

                    if (isAddressValid) {
                        residenza.value?.let { updates["residenza"] = it }
                        cap.value?.let { updates["cap"] = it }
                        addressVerified.postValue(true) // Notifica che l'indirizzo è verificato
                    } else {
                        message.postValue("Residenza o CAP non validi.")
                        addressVerified.postValue(false) // Notifica che l'indirizzo non è verificato
                        return@launch
                    }

                    // Esegui l'aggiornamento del documento
                    utentiCollection.document(userId).update(updates).await()
                    message.postValue("Profilo salvato con successo.")
                } else {
                    message.postValue("Tutti i campi devono essere compilati.")
                    addressVerified.postValue(false) // Se i campi non sono validi
                }
            } catch (e: Exception) {
                message.postValue("Errore nel salvataggio del profilo utente: ${e.message}")
                addressVerified.postValue(false)
            }
        }
    }

    suspend fun verificaIndirizzo(indirizzo: String): Boolean {
        return withContext(Dispatchers.IO) {
            val call = RetrofitInstance.api.getLocation(indirizzo)
            try {
                val response = call.execute()
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val location = response.body()!![0]
                    val displayName = location.display_name.lowercase()
                    val capValido = displayName.contains(cap.value!!)
                    val residenzaValida = displayName.contains(residenza.value!!.lowercase())

                    return@withContext capValido && residenzaValida
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext false
        }
    }

    fun eliminaDatiUtenteDaFirestore(userId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val batch = db.batch()

                    val userDocRef = db.collection("utenti").document(userId)
                    batch.delete(userDocRef)

                    if (ruolo) {
                        val annunci = db.collection("annunci")
                            .whereEqualTo("tutor", db.document("utenti/$userId")).get().await()
                        for (annuncio in annunci.documents) {
                            batch.delete(annuncio.reference)
                        }

                        val prenotazioni = db.collection("prenotazioni")
                            .whereEqualTo("tutor", db.document("utenti/$userId")).get().await()
                        for (prenotazione in prenotazioni.documents) {
                            batch.delete(prenotazione.reference)
                        }

                        val calendario = db.collection("calendario")
                            .whereEqualTo("tutorRef", db.document("utenti/$userId")).get().await()
                        for (fascia in calendario.documents) {
                            batch.delete(fascia.reference)
                        }
                    } else {
                        message.postValue("ANCORA DA IMPLEMENTARE")
                    }

                    batch.commit().await()

                    user.delete().await()

                    message.postValue("Account e dati associati eliminati con successo.")

                } catch (e: Exception) {
                    message.postValue("Errore durante l'eliminazione dell'account: ${e.message}")
                }
            }
        }
    }
}
