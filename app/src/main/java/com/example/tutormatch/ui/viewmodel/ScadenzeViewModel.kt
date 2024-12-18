package com.example.tutormatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.util.FirebaseUtil.eliminaFasceOrarieScadute
import com.example.tutormatch.util.FirebaseUtil.eliminaPrenotazioniScadute
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ScadenzeViewModel : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())

    // LiveData per inviare messaggi pop-up alla MainActivity
    private val _popupMessage = MutableLiveData<String>()
    val popupMessage: LiveData<String> = _popupMessage

    var operazioneEseguita = false
        private set


    fun gestisciScadenze() {
        viewModelScope.launch {
            eseguiOperazioneAlleProssimaOra()
        }
    }

    // Funzione per eseguire le operazioni alla prossima ora
    fun eseguiOperazioneAlleProssimaOra() {
        val calendarRome = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
        val minutiCorrenti = calendarRome.get(Calendar.MINUTE)
        val secondiCorrenti = calendarRome.get(Calendar.SECOND)

        // Calcola quanti minuti e secondi mancano all'ora successiva
        val minutiMancanti = 60 - minutiCorrenti
        val millisecondiMancanti = (minutiMancanti * 60 - secondiCorrenti) * 1000L

        eseguiOperazioniPeriodiche()
        // Esegui l'operazione quando scatta l'ora successiva
        handler.postDelayed({
            eseguiOperazioniPeriodiche()

            // Dopo l'esecuzione calcolata, esegui ogni 60 minuti (3600000 millisecondi)
            handler.postDelayed(object : Runnable {
                override fun run() {
                    eseguiOperazioniPeriodiche()
                    handler.postDelayed(this, 60 * 60 * 1000L) // Ripeti ogni 60 minuti
                }
            }, 60 * 60 * 1000L)

        }, millisecondiMancanti)
    }

    // Funzione che gestisce le operazioni periodiche
    private fun eseguiOperazioniPeriodiche() {
        viewModelScope.launch {
            try {
                operazioneEseguita = true
                // Sposta le operazioni di I/O su un thread dedicato
                withContext(Dispatchers.IO) {
                    val calendarRome = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
                    val nowRome = calendarRome.time

                    // Formattazione per ottenere la sola data e ora
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).apply {
                        timeZone = TimeZone.getTimeZone("Europe/Rome")
                    }
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALY).apply {
                        timeZone = TimeZone.getTimeZone("Europe/Rome")
                    }
                    val dataCorrente = dateFormat.format(nowRome)
                    val oraCorrente = timeFormat.format(nowRome)

                    // Esecuzione sequenziale delle funzioni suspend
                    eliminaFasceOrarieScadute(dataCorrente, oraCorrente)  // Prima funzione
                    eliminaPrenotazioniScadute()  // Seconda funzione, eseguita solo dopo che la prima è terminata
                }
            } catch (e: Exception) {
                // Invia un messaggio pop-up di errore
                _popupMessage.postValue("Errore durante l'operazione: ${e.message}")
            }
        }
    }
}