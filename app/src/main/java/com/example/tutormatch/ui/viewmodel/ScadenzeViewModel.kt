package com.example.tutormatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Prenotazione
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ScadenzeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Funzione principale per gestire le scadenze del calendario e delle prenotazioni
    fun gestisciScadenze() {
        viewModelScope.launch {
            // Ottieni il tempo corrente con fuso orario di Roma
            val calendarRome = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
            val nowRome = calendarRome.time

            // Formattazione per ottenere la sola data nel formato giorno-mese-anno (senza orario)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).apply {
                timeZone = TimeZone.getTimeZone("Europe/Rome")  // Usa il fuso orario italiano
            }

            // Data corrente formattata come yyyy-MM-dd (senza orario)
            val dataCorrente = dateFormat.format(nowRome)

            // Formattazione per ottenere l'ora corrente nel formato HH:mm
            val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALY).apply {
                timeZone = TimeZone.getTimeZone("Europe/Rome")
            }
            val oraCorrente = timeFormat.format(nowRome)

            // 1. Controllo e rimozione delle voci scadute nel calendario
            db.collection("calendario")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.forEach { document ->
                        val calendario = document.toObject(Calendario::class.java)

                        // Formatta la data dell'oggetto Calendario (ignorando l'orario)
                        val calendarioDataFormattata = dateFormat.format(calendario.data)

                        // Confronto della sola data: se la data del calendario è precedente a quella corrente, elimina
                        if (calendarioDataFormattata < dataCorrente) {
                            eliminaDocumento(document.id, "calendario")
                        }
                        // Se la data è uguale al giorno corrente, confronta l'ora
                        else if (calendarioDataFormattata == dataCorrente && calendario.oraInizio < oraCorrente) {
                            eliminaDocumento(document.id, "calendario")
                        }
                    }
                }
                .addOnFailureListener {
                    // Gestisci eventuali errori nell'accesso a Firestore
                }

            // 2. Controllo e rimozione delle prenotazioni scadute
            db.collection("prenotazioni")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.forEach { document ->
                        val prenotazione = document.toObject(Prenotazione::class.java)

                        // Verifica se la fascia oraria associata (calendario) esiste ancora
                        prenotazione.fasciaCalendarioRef?.get()
                            ?.addOnFailureListener {
                                // Se il documento non esiste più, elimina la prenotazione
                                eliminaDocumento(document.id, "prenotazioni")
                            }
                            ?.addOnSuccessListener { fasciaDoc ->
                                // Se esiste, non fare nulla
                                // Puoi gestire qui eventuali altre logiche se necessario
                            }
                    }
                }
                .addOnFailureListener {
                    // Gestisci eventuali errori nell'accesso a Firestore
                }

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
