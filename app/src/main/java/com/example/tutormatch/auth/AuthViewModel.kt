package com.example.tutormatch.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.network.RetrofitInstance
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private var _firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData per i campi di input
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val nome = MutableLiveData<String>()
    val cognome = MutableLiveData<String>()
    val residenza = MutableLiveData<String>()
    val via = MutableLiveData<String>()
    val cap = MutableLiveData<String>()
    private val _ruolo = MutableLiveData<Boolean>()

    private val _passwordResetMessage = MutableLiveData<String?>()
    val passwordResetMessage: LiveData<String?> = _passwordResetMessage

    // LiveData per mostrare messaggi all'utente
    private val _showMessage = MutableLiveData<String?>()
    val showMessage: LiveData<String?> = _showMessage

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    // LiveData per l'utente corrente
    val utente = MutableLiveData<Utente>()

    fun setRuolo(ruolo: Boolean) {
        _ruolo.value = ruolo
    }

    // Metodo chiamato quando l'utente preme il pulsante di registrazione
    fun onRegisterClick() {

        // Controlla che tutti i campi richiesti siano presenti e non vuoti
        if (email.value.isNullOrEmpty() || password.value.isNullOrEmpty() ||
            nome.value.isNullOrEmpty() || cognome.value.isNullOrEmpty() ||
            via.value.isNullOrEmpty() || cap.value.isNullOrEmpty() ||
            residenza.value.isNullOrEmpty()) {

            _showMessage.value = "Tutti i campi devono essere compilati"
            return
        }

        // Prima di registrare l'utente, controlla la validità dell'indirizzo
        val indirizzo = "${cap.value}, ${residenza.value}, ${via.value}"
        val indirizzoSenzaVia = "${cap.value}, ${residenza.value}"
        viewModelScope.launch {
            val flag = verificaIndirizzo(indirizzo, indirizzoSenzaVia)
            if (flag) {
                registraUtente(email.value!!, password.value!!)
            } else {
                _showMessage.value = "Verifica che residenza e CAP siano corretti."
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
                            _showMessage.postValue("Via non trovata, registrazione con indirizzo senza via")
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

    //return@withContext true serve a specificare che true è il valore di ritorno del blocco withContext,
    // il che è particolarmente utile quando lavori con funzioni di ordine superiore
    // o in contesti di coroutine dove vuoi gestire esplicitamente il flusso di ritorno all'interno di un lambda.



    // Funzione per registrare l'utente su Firebase
    private fun registraUtente(email: String, password: String) {
        _firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = _firebaseAuth.currentUser
                    val newUser = Utente(
                        userId = user!!.uid,
                        email = email,
                        nome = nome.value!!,
                        cognome = cognome.value!!,
                        residenza = residenza.value!!,
                        via = via.value!!,
                        cap = cap.value!!,
                        ruolo = _ruolo.value!!
                    )
                    user.let {
                        FirebaseUtil.addUserToFirestore(newUser)
                        _showMessage.value = "Registrazione riuscita!"
                        _navigateBack.value = true
                    }
                } else {
                    _showMessage.value = task.exception?.message ?: "Registrazione fallita"
                }
            }
    }

    // Metodo chiamato quando l'utente preme il pulsante di login
    fun onLoginClick() {

        // Controllo dei campi obbligatori
        if (email.value.isNullOrEmpty() || password.value.isNullOrEmpty()) {
            _showMessage.value = "Tutti i campi devono essere compilati"
            return
        }

        // Avvia una coroutine per eseguire le operazioni asincrone
        viewModelScope.launch {
            try {
                // Effettua il login in modo asincrono
                val result = _firebaseAuth.signInWithEmailAndPassword(email.value!!, password.value!!).await()

                if (result.user != null) {
                    val user = result.user

                    // Usa la funzione suspend per ottenere l'utente da Firestore
                    val utente = FirebaseUtil.getUserFromFirestore(user!!.uid)

                    // Aggiorna il LiveData con l'utente
                    this@AuthViewModel.utente.value = utente
                    _showMessage.value = "Login riuscito!"
                } else {
                    _showMessage.value = "Login fallito"
                }
            } catch (e: Exception) {
                // Gestisci eventuali errori
                _showMessage.value = "Credenziali errate"
            }
        }
    }


    fun onForgotPasswordClick() {
        val emailValue = email.value

        if (emailValue.isNullOrEmpty()) {
            _showMessage.value = "Per favore, inserisci un'email."
            return
        }

        _firebaseAuth.sendPasswordResetEmail(emailValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _showMessage.value = "Email di recupero password inviata!"
                } else {
                    _showMessage.value = task.exception?.message ?: "Invio email fallito"
                }
            }
    }

    fun resetPasswordResetMessage() {
        _passwordResetMessage.value = null
    }

    fun onBackClick() {
        _navigateBack.value = true
    }

    fun onNavigatedBack() {
        _navigateBack.value = false
    }
}
