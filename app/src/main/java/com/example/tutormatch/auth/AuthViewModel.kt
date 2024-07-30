package com.example.tutormatch.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth

// ViewModel per gestire l'autenticazione e le operazioni relative agli utenti
class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()  // Istanza di FirebaseAuth

    // LiveData per i campi di input
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val nome = MutableLiveData<String>()
    val cognome = MutableLiveData<String>()
    val residenza = MutableLiveData<String>()
    val via = MutableLiveData<String>()
    val civico = MutableLiveData<String>()
    private val _ruolo = MutableLiveData<Boolean>()

    // LiveData per mostrare messaggi all'utente
    private val _showMessage = MutableLiveData<String?>()
    val showMessage: LiveData<String?> = _showMessage

    private val _passwordResetMessage = MutableLiveData<String?>()
    val passwordResetMessage: LiveData<String?> = _passwordResetMessage

    // LiveData per gestire la navigazione
    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    // LiveData per l'utente corrente
    val utente = MutableLiveData<Utente>()

    // Metodo per impostare il ruolo dell'utente
    fun setRuolo(ruolo: Boolean) {
        _ruolo.value = ruolo
    }

    // Metodo chiamato quando l'utente preme il pulsante di registrazione
    fun onRegisterClick() {
        val emailValue = email.value ?: return
        val passwordValue = password.value ?: return

        // Creazione di un nuovo utente con email e password
        auth.createUserWithEmailAndPassword(emailValue, passwordValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val newUser = Utente(
                        userId = user?.uid ?: "", // Imposta l'ID utente come l'UID di FirebaseAuth
                        email = emailValue,
                        nome = nome.value ?: "",
                        cognome = cognome.value ?: "",
                        residenza = residenza.value ?: "",
                        via = via.value ?: "",
                        civico = civico.value ?: "",
                        ruolo = _ruolo.value ?: false
                    )
                    user?.let {
                        // Aggiunge il nuovo utente a Firestore
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
        val emailValue = email.value ?: return
        val passwordValue = password.value ?: return

        // Login con email e password
        auth.signInWithEmailAndPassword(emailValue, passwordValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Recupera i dati dell'utente da Firestore
                        FirebaseUtil.getUserFromFirestore(it.uid) { utente ->
                            this.utente.value = utente
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
            _passwordResetMessage.value = "Per favore, inserisci un'email."
            return
        }

        auth.sendPasswordResetEmail(emailValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _passwordResetMessage.value = "Email di recupero password inviata!"
                } else {
                    _passwordResetMessage.value = task.exception?.message ?: "Invio email fallito"
                }
            }
    }

    fun resetPasswordResetMessage() {
        _passwordResetMessage.value = null
    }

    fun resetShowMessage() {
        _showMessage.value = null
    }

    // Metodo chiamato quando l'utente preme il pulsante per tornare indietro
    fun onBackClick() {
        _navigateBack.value = true
    }

    // Resetta il flag di navigazione
    fun onNavigatedBack() {
        _navigateBack.value = false
    }
}

