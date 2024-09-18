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
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

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

        viewModelScope.launch {
            val flag = verificaIndirizzo(indirizzo)
            if (flag) {
                registraUtente(email.value!!, password.value!!)
            } else {
                _showMessage.value = "Verifica che residenza e CAP siano corretti."
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
    //return@withContext true serve a specificare che true è il valore di ritorno del blocco withContext,
    // il che è particolarmente utile quando lavori con funzioni di ordine superiore
    // o in contesti di coroutine dove vuoi gestire esplicitamente il flusso di ritorno all'interno di un lambda.



    // Funzione per registrare l'utente su Firebase
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
                        ruolo = _ruolo.value!!,
                        fcmToken = null // Token FCM sarà aggiornato successivamente
                    )
                    user.let {
                        // Aggiungi l'utente a Firestore
                        FirebaseUtil.addUserToFirestore(newUser)

                        // Ora ottieni il token FCM e salvalo su Firestore
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { taskToken ->
                            if (taskToken.isSuccessful) {
                                val token = taskToken.result
                                if (token != null) {
                                    FirebaseUtil.saveUserFcmToken(email, token)
                                }
                            }
                        }

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

        _firebaseAuth.signInWithEmailAndPassword(email.value!!, password.value!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = _firebaseAuth.currentUser
                    user?.let {
                        // Ottieni i dettagli dell'utente da Firestore
                        FirebaseUtil.getUserFromFirestore(it.uid) { utente ->
                            this.utente.value = utente

                            // Una volta effettuato il login, aggiorna il token FCM
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { taskToken ->
                                if (taskToken.isSuccessful) {
                                    val token = taskToken.result
                                    if (token != null) {
                                        // Usa l'uid dell'utente invece dell'email per aggiornare il token FCM
                                        FirebaseUtil.saveUserFcmToken(it.uid, token)
                                    }
                                }
                            }

                            _showMessage.value = "Login riuscito!"
                        }
                    }
                } else {
                    _showMessage.value = task.exception?.message ?: "Login fallito"
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
