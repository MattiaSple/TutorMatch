package com.example.tutormatch.ui.view.activity

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tutormatch.R
import com.example.tutormatch.auth.AuthActivity
import com.example.tutormatch.util.FirebaseUtil

class MainActivity : AppCompatActivity() {

    // Codice per il permesso di notifica
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Verifica e richiede il permesso di notifica per Android 13+
        checkNotificationPermission()
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

    // Funzione per verificare e richiedere il permesso di notifiche su Android 13+
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Richiedi il permesso di notifiche
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // Gestisci la risposta dell'utente al dialogo del permesso
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permesso di inviare notifiche concesso")
            } else {
                Log.d(TAG, "Permesso di inviare notifiche negato")
            }
        }
    }
}
