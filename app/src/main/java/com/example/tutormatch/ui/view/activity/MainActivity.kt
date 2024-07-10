package com.example.tutormatch.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tutormatch.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    // Funzione per navigare alla seconda attività con un extra booleano
    private fun navigateToSecondActivity(type: Boolean){
        val intent = Intent(this, RegistrationActivity::class.java).apply {
            putExtra("EXTRA_BOOLEAN", type)
        }
        startActivity(intent)
    }
}