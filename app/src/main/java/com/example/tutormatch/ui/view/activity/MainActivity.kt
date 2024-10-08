package com.example.tutormatch.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.R
import com.example.tutormatch.auth.AuthActivity
import com.example.tutormatch.viewmodel.ScadenzeViewModel
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var scadenzeViewModel: ScadenzeViewModel

    // Handler per eseguire il ciclo periodico
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inizializza il ViewModel
        scadenzeViewModel = ViewModelProvider(this)[ScadenzeViewModel::class.java]

        // Esegui subito l'operazione
        scadenzeViewModel.gestisciScadenze()

        // Calcola i minuti mancanti all'ora successiva e avvia il ciclo
        eseguiOperazioneAlleProssimaOra()
    }

    // Funzione per calcolare i minuti mancanti all'ora successiva
    private fun eseguiOperazioneAlleProssimaOra() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
        val minutiCorrenti = calendar.get(Calendar.MINUTE)
        val secondiCorrenti = calendar.get(Calendar.SECOND)

        // Calcola quanti minuti e secondi mancano all'ora successiva
        val minutiMancanti = 60 - minutiCorrenti
        val millisecondiMancanti = (minutiMancanti * 60 - secondiCorrenti) * 1000L

        // Esegui l'operazione quando scatta l'ora successiva
        handler.postDelayed({
            // Esegui l'operazione all'inizio della prossima ora
            scadenzeViewModel.gestisciScadenze()

            // Dopo la prima esecuzione, esegui ogni 60 minuti (3600000 millisecondi)
            handler.postDelayed(object : Runnable {
                override fun run() {
                    scadenzeViewModel.gestisciScadenze()
                    handler.postDelayed(this, 60 * 60 * 1000L) // Ripeti ogni 60 minuti
                }
            }, 60 * 60 * 1000L)

        }, millisecondiMancanti)
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