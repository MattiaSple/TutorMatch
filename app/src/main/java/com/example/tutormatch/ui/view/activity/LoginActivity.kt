package com.example.tutormatch.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.R
import com.example.tutormatch.databinding.ActivityAccediBinding
import com.example.tutormatch.ui.viewmodel.RegistrationViewModel
import com.example.tutormatch.ui.viewmodel.UtenteViewModel


class LoginActivity : AppCompatActivity() {
    lateinit var registrationViewModel: RegistrationViewModel
    private val utenteViewModel: UtenteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inizializza il ViewModel prima di utilizzarlo nel binding
        registrationViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(RegistrationViewModel::class.java)

        val binding: ActivityAccediBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_accedi
        )

        binding.viewModel = registrationViewModel
        binding.lifecycleOwner = this

        // Osserva il LiveData navigateBack per gestire la navigazione
        registrationViewModel.navigateBack.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish() // Chiude l'Activity corrente
                registrationViewModel.onNavigatedBack()
            }
        })

        // Osserva showMessage per mostrare il messaggio e navigare
        registrationViewModel.showMessage.observe(this, Observer { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (it == "Login riuscito!") {
                    val utente = registrationViewModel.utente.value
                    if (utente != null) {
                        utenteViewModel.setUserData(utente)
                        val intent = if (!utente.ruolo) {
                            Intent(this, HomeStudentActivity::class.java)
                        } else {
                            Intent(this, HomeTutorActivity::class.java)
                        }
                        startActivity(intent)
                        //finish() // Chiude l'Activity corrente
                    }
                }
            }
        })
    }
}
