package com.example.tutormatch.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.R
import com.example.tutormatch.databinding.ActivityAccediBinding
import com.example.tutormatch.auth.AuthViewModel


class LoginActivity : AppCompatActivity() {
    lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il ViewModel prima di utilizzarlo nel binding
        authViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[AuthViewModel::class.java]

        val binding: ActivityAccediBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_accedi
        )

        binding.viewModel = authViewModel
        binding.lifecycleOwner = this

        // Osserva il LiveData navigateBack per gestire la navigazione
        authViewModel.navigateBack.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish() // Chiude l'Activity corrente
                authViewModel.onNavigatedBack()
            }
        })

        // Osserva showMessage per mostrare il messaggio e navigare
        authViewModel.showMessage.observe(this, Observer { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (it == "Login riuscito!") {
                    val utente = authViewModel.utente.value
                    if (utente != null) {
                        val intent = Intent(this, HomeActivity::class.java)
                        // Aggiungi i dati dell'utente come extra dell'intent
                        intent.putExtra("userId", utente.userId)
                        intent.putExtra("nome", utente.nome)
                        intent.putExtra("cognome", utente.cognome)
                        intent.putExtra("ruolo", utente.ruolo)

                        startActivity(intent)
                        finish() // Chiude l'Activity corrente
                    }
                }
            }
        })

        // Osserva passwordResetMessage per mostrare i messaggi di recupero password
        authViewModel.passwordResetMessage.observe(this, Observer { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                authViewModel.resetPasswordResetMessage() // Resetta il messaggio dopo averlo mostrato
            }
        })
    }
}