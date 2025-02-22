package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.network.RetrofitInstance
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfiloViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData per i dettagli del profilo utente
    val nome = MutableLiveData<String>() // Nome dell'utente
    val cognome = MutableLiveData<String>() // Cognome dell'utente
    val residenza = MutableLiveData<String>() // Residenza dell'utente (es. città)
    val via = MutableLiveData<String>() // Via dell'utente
    val cap = MutableLiveData<String>() // CAP (Codice di Avviamento Postale) dell'utente
    val message = MutableLiveData<String>() // Messaggi di stato o errore

    // LiveData per la navigazione verso la schermata principale
    private val _navigateToMain = MutableLiveData<Boolean>() // Indica se navigare alla schermata principale
    val navigateToMain: LiveData<Boolean> = _navigateToMain // Espone un LiveData immutabile per l'osservazione

    // LiveData per informazioni aggiuntive del profilo utente
    val mediaValutazioni = MutableLiveData<String>() // Media delle valutazioni dell'utente (se è un tutor)
    val isTutor = MutableLiveData<Boolean>() // Indica se l'utente ha il ruolo di tutor
    val addressVerified = MutableLiveData<Boolean>() // Indica se l'indirizzo dell'utente è stato verificato

    // LiveData per mostrare messaggi all'utente
    private val _showMessage = MutableLiveData<String?>() // Messaggi di feedback all'utente
    val showMessage: LiveData<String?> = _showMessage // Espone un LiveData immutabile per l'osservazione

    // Variabile per il ruolo dell'utente
    private var ruolo: Boolean = false // Variabile che indica il ruolo (es. studente o tutor)


    // Carica i dati del profilo utente
    fun loadUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val utente = FirebaseUtil.getUserFromFirestore(userId)
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
                    } else {
                        mediaValutazioni.postValue("Valutazione: 0")
                    }
                } ?: run {
                    message.postValue("Errore nel caricamento del profilo utente.")
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
                    val indirizzoCompleto = "${cap.value}, ${residenza.value}, ${via.value}"
                    val isAddressValid = verificaIndirizzo(indirizzoCompleto,indirizzoSenzaVia)

                    if (isAddressValid) {
                        residenza.value?.let { updates["residenza"] = it }
                        cap.value?.let { updates["cap"] = it }
                        addressVerified.postValue(true) // Notifica che l'indirizzo è verificato
                    } else {
                        message.postValue("Residenza o CAP non validi.")
                        addressVerified.postValue(false) // Notifica che l'indirizzo non è verificato
                        return@launch
                    }

                    // Aggiorna i dati dell'utente su Firestore
                    val success = FirebaseUtil.updateUserInFirestore(userId, updates)
                    if (success) {
                        message.postValue("Profilo salvato con successo.")
                    } else {
                        message.postValue("Errore durante l'aggiornamento del profilo.")
                    }

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

    private suspend fun verificaIndirizzo(indirizzo: String, indirizzoSenzaVia: String): Boolean {
        return withContext(Dispatchers.IO) {
            val callCompleto = RetrofitInstance.api.getLocation(indirizzo)
            try {
                val responseCompleto = callCompleto.execute()
                if (responseCompleto.isSuccessful && responseCompleto.body()?.isNotEmpty() == true) {
                    val location = responseCompleto.body()!!
                    for(localita in location)
                    {
                        val address = localita.address
                        // Verifica che CAP, città e via siano validi
                        val capValido = address?.postcode?.equals(cap.value, ignoreCase = true) ?: false
                        val cittaValida = address?.city?.equals(residenza.value, ignoreCase = true)
                            ?: address?.town?.equals(residenza.value, ignoreCase = true)
                            ?: address?.village?.equals(residenza.value, ignoreCase = true)
                            ?: false
                        val viaValida = address?.road?.contains(via.value!!, ignoreCase = true) ?: false

                        // Se tutti e tre i parametri corrispondono, l'indirizzo è valido
                        if (capValido && cittaValida && viaValida) {
                            return@withContext true
                        }
                    }
                }

                // Se l'indirizzo completo non è valido, prova con l'indirizzo senza via
                val callSenzaVia = RetrofitInstance.api.getLocation(indirizzoSenzaVia)
                val responseSenzaVia = callSenzaVia.execute()
                if (responseSenzaVia.isSuccessful && responseSenzaVia.body()?.isNotEmpty() == true) {

                    val locationSenzaVia = responseSenzaVia.body()!!
                    for(localita in locationSenzaVia)
                    {
                        val addressSenzaVia = localita.address
                        val capValidoSenzaVia = addressSenzaVia?.postcode?.equals(cap.value, ignoreCase = true) ?: false
                        val residenzaValidaSenzaVia = addressSenzaVia?.city?.equals(residenza.value, ignoreCase = true)
                            ?: addressSenzaVia?.town?.equals(residenza.value, ignoreCase = true)
                            ?: addressSenzaVia?.village?.equals(residenza.value, ignoreCase = true)
                            ?: false

                        // Se CAP e residenza corrispondono, ritorna true e informa che la via non è stata trovata
                        if (capValidoSenzaVia && residenzaValidaSenzaVia) {
                            _showMessage.postValue("Via non trovata, salvataggio senza via")
                            return@withContext true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext false
        }
    }

    fun eliminaDatiUtenteDaFirestore(userId: String, ruolo: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {

            // Verifica la presenza di prenotazioni
            val hasReservations = FirebaseUtil.hasReservations(userId, ruolo)

            withContext(Dispatchers.Main) {
                if (hasReservations) {
                    // Messaggio di avviso per informare l'utente di eliminare prima le prenotazioni
                    _showMessage.postValue("Disdici le prenotazioni prima!")
                } else {
                    // Procedi con l'eliminazione dell'account
                    val success = FirebaseUtil.eliminaUtenteCompletamente(ruolo)
                    if (success) {
                        _showMessage.postValue("Account e dati associati eliminati con successo.")
                        _navigateToMain.postValue(true)
                    } else {
                        _showMessage.postValue("Errore durante l'eliminazione dell'account o dei dati associati.")
                    }
                }
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        _navigateToMain.postValue(true)  // Notifica alla UI di tornare alla MainActivity
    }

}
