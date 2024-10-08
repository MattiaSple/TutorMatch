package com.example.tutormatch.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ScadenzeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

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

            // Chiamata alla funzione atomica in FirebaseUtil
            FirebaseUtil.gestisciScadenzeAtomiche(
                dataCorrente = dataCorrente,
                oraCorrente = oraCorrente,
                onSuccess = {
                    Log.e("VAFFANCULO", "DIO PORCO FUNZIONA")
                },
                onFailure = { exception ->
                    // Gestione degli errori
                }
            )
        }
    }


    // Funzione per eliminare un documento da una collezione specifica
    private fun eliminaDocumento(documentId: String, collezione: String) {
        db.collection(collezione).document(documentId).delete()
            .addOnSuccessListener {
                // Documento eliminato con successo
            }
            .addOnFailureListener {
                // Gestisci eventuali errori durante l'eliminazione
            }
    }
}
