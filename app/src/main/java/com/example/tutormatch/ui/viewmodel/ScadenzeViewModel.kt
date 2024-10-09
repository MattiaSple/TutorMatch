package com.example.tutormatch.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.util.FirebaseUtil.eliminaFasceOrarieScadute
import com.example.tutormatch.util.FirebaseUtil.eliminaPrenotazioniScadute
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ScadenzeViewModel : ViewModel() {

    fun gestisciScadenze() {
        viewModelScope.launch {
            // Ottieni il tempo corrente con fuso orario di Roma
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

            try {
                // 1. Prima elimina le fasce orarie scadute
                eliminaFasceOrarieScadute(dataCorrente, oraCorrente)

                // 2. Poi elimina le prenotazioni scadute
                eliminaPrenotazioniScadute()

                // Se tutto ha successo, puoi loggare un messaggio
                Log.i("SUCCESS", "Operazioni completate con successo")

            } catch (exception: Exception) {
                // Gestione degli errori
                Log.e("ERROR", "Errore durante le operazioni: ${exception.message}")
            }
        }
    }
}
