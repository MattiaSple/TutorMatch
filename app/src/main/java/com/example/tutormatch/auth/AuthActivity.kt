package com.example.tutormatch.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.R
import com.example.tutormatch.databinding.ActivityRegistratiBinding
import com.example.tutormatch.ui.view.activity.MainActivity

class AuthActivity : AppCompatActivity() {

    lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il ViewModel prima di utilizzarlo nel binding
        authViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(AuthViewModel::class.java)

        // Ottieni il binding
        val binding: ActivityRegistratiBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_registrati
        )

        // Collega il ViewModel al layout
        binding.viewModel = authViewModel
        binding.lifecycleOwner = this

        val ruolo = intent.getBooleanExtra("EXTRA_BOOLEAN", false)
        authViewModel.setRuolo(ruolo)
        if(!ruolo)
        {
            binding.registrazione.text = "Crea un account studente"
        }

        // Osserva il LiveData showMessage per mostrare il Toast
        authViewModel.showMessage.observe(this, Observer { message ->
            message?.let {
                Log.d("AuthActivity", "showMessage observed: $it")
                // Disabilita il bottone
                binding.registrazione.isEnabled = false

                // Mostra il Toast
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()

                // Usa un Handler per riabilitare il bottone dopo il ritardo specificato
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.registrazione.isEnabled = true
                }, 2500)
            }
        })

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
    }
}
