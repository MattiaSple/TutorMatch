package com.example.tutormatch.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.R
import com.example.tutormatch.auth.AuthActivity
import com.example.tutormatch.viewmodel.ScadenzeViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var scadenzeViewModel: ScadenzeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inizializza il ViewModel
        scadenzeViewModel = ViewModelProvider(this)[ScadenzeViewModel::class.java]

        // Osserva il LiveData per i messaggi pop-up
        scadenzeViewModel.popupMessage.observe(this) { message ->
            message?.let {
                // Mostra il pop-up
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Esegui subito l'operazione
        scadenzeViewModel.gestisciScadenze()
    }

    // Funzione chiamata quando si clicca il pulsante "Tutor"
    fun navigateToTutor(v: View) {
        navigateToSecondActivity(true)
    }

    // Funzione chiamata quando si clicca il pulsante "Studente"
    fun navigateToStudent(v: View) {
        navigateToSecondActivity(false)
    }

    // Funzione chiamata quando si clicca il pulsante "Accedi"
    fun accedi(v: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    // Funzione per navigare alla seconda attività con un extra booleano
    private fun navigateToSecondActivity(type: Boolean) {
        val intent = Intent(this, AuthActivity::class.java).apply {
            putExtra("EXTRA_BOOLEAN", type)
        }
        startActivity(intent)
    }
}