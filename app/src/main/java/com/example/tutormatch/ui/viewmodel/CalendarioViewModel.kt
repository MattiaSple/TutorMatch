package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarioViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData per la lista delle disponibilità del tutor
    private val _lista_disponibilita = MutableLiveData<List<Calendario>>()
    val lista_disponibilita: LiveData<List<Calendario>> get() = _lista_disponibilita

    // LiveData per mostrare messaggi di errore o successo
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // LiveData per gestire data e orari della disponibilità
    val data = MutableLiveData<String>()
    val oraInizio = MutableLiveData<String>()
    val oraFine = MutableLiveData<String>()
    val statoPren = MutableLiveData<Boolean>()

    // Riferimento al documento del tutor in Firestore
    private lateinit var _tutorRef: DocumentReference

    // Callback per aggiornare gli orari di inizio
    private var updateOrariInizioCallback: (() -> Unit)? = null

    // Imposta il callback per aggiornare gli orari di inizio
    fun setUpdateOrariInizioCallback(callback: () -> Unit) {
        updateOrariInizioCallback = callback
    }

    // Imposta il riferimento al tutor e carica le sue disponibilità
    fun setTutorReference(tutorRef: String) {
        viewModelScope.launch {
            try {
                _tutorRef = FirebaseUtil.getDocumentRefById(tutorRef) // Recupera il riferimento del tutor
                loadDisponibilita() // Carica le disponibilità del tutor
            } catch (e: Exception) {
                _message.postValue("Errore nel recupero del tutor: ${e.message}")
            }
        }
    }

    // Carica le disponibilità del tutor dal database
    fun loadDisponibilita() {
        viewModelScope.launch {
            try {
                val loadedDisponibilita = FirebaseUtil.loadDisponibilita(_tutorRef)
                withContext(Dispatchers.Main) {
                    _lista_disponibilita.value = loadedDisponibilita
                    updateOrariInizioCallback?.invoke() // Aggiorna il callback per gli orari di inizio
                }
            } catch (e: Exception) {
                _message.postValue("Errore nel caricamento delle fasce orarie.")
            }
        }
    }

    // Carica le disponibilità per una data specifica e restituisce gli orari esistenti tramite callback
    fun caricaDisponibilitaPerData(data: String, callback: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val existingOrari = FirebaseUtil.caricaDisponibilitaPerData(_tutorRef, data)
                callback(existingOrari) // Restituisce gli orari esistenti
            } catch (e: Exception) {
                callback(emptyList()) // Restituisce una lista vuota in caso di errore
            }
        }
    }

    // Genera una lista di orari disponibili, escludendo quelli già occupati
    fun generateOrari(existingOrari: List<String>): List<String> {
        val orari = mutableListOf<String>()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))  // Imposta il fuso orario di Roma
        val dateFormat = SimpleDateFormat("HH:mm", Locale.ITALY).apply {
            timeZone = TimeZone.getTimeZone("Europe/Rome")  // Usa il fuso orario italiano
        }

        var oreRimanenti = 24
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)


        var counter = 0
        while (counter < oreRimanenti) {
            val orario = dateFormat.format(calendar.time)
            if (!existingOrari.contains(orario)) {
                orari.add(orario)
            }
            calendar.add(Calendar.MINUTE, 60)
            counter++
        }
        return orari
    }

    // Salva una nuova disponibilità per il tutor
    suspend fun salvaDisponibilita(): Boolean {
        val dataVal = data.value?.trim() ?: ""
        val oraInizioVal = oraInizio.value ?: ""
        val oraFineVal = oraFine.value ?: ""
        val statoPrenVal = statoPren.value ?: false

        // Controlla che i campi obbligatori siano riempiti
        if (dataVal.isBlank() || oraInizioVal.isBlank() || oraFineVal.isBlank()) {
            _message.value = "Data, Ora Inizio e Ora Fine sono necessari!"
            return false
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dataParsed = dateFormat.parse(dataVal)
        val inizioParsed = timeFormat.parse(oraInizioVal)
        val fineParsed = timeFormat.parse(oraFineVal)

        // Calcola le fasce orarie da salvare
        val disponibilitaList = mutableListOf<Calendario>()
        val calendar = Calendar.getInstance().apply { time = inizioParsed }

        if (fineParsed.before(inizioParsed) || fineParsed == inizioParsed) {
            val calendarFine = Calendar.getInstance().apply {
                time = fineParsed
                add(Calendar.DAY_OF_MONTH, 1)
            }
            fineParsed.time = calendarFine.timeInMillis
        }

        while (calendar.time.before(fineParsed)) {
            val oraInizioStr = timeFormat.format(calendar.time)
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            val oraFineStr = timeFormat.format(calendar.time)

            if (calendar.time.after(fineParsed)) {
                break
            }

            disponibilitaList.add(
                Calendario(
                    tutorRef = _tutorRef,
                    data = dataParsed,
                    oraInizio = oraInizioStr,
                    oraFine = oraFineStr,
                    statoPren = statoPrenVal
                )
            )
        }

        return try {
            // Salva le fasce orarie nel database
            withContext(Dispatchers.IO) {
                FirebaseUtil.salvaDisponibilita(disponibilitaList)
            }
            withContext(Dispatchers.Main) {
                _message.value = "Disponibilità salvate con successo"
                loadDisponibilita() // Ricarica le disponibilità
            }
            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _message.value = "Errore nel salvataggio della disponibilità: ${e.message}"
            }
            false
        }
    }


    // Elimina una disponibilità esistente
    fun eliminaDisponibilita(calendario: Calendario) {
        if (calendario.statoPren) { // Controlla se la fascia è già prenotata
            _message.postValue("Eliminare prima la prenotazione\nAvvisa lo studente!")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = FirebaseUtil.eliminaDisponibilita(calendario)
                withContext(Dispatchers.Main) {
                    if (success) {
                        _message.postValue("Disponibilità eliminata con successo")
                    } else {
                        _message.postValue("Errore nell'eliminazione della disponibilità")
                    }
                }
                loadDisponibilita() // Aggiorna la lista delle disponibilità
            } catch (e: Exception) {
                _message.postValue("Errore nell'eliminazione della disponibilità: ${e.message}")
            }
        }
    }

    // Ottiene il riferimento del tutor a partire dall'ID di un annuncio
    suspend fun getTutorDaAnnuncio(annuncioId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Chiamata a FirebaseUtil per ottenere il tutorRef
                val tutorRef = FirebaseUtil.getTutorDaAnnuncioF(annuncioId)
                if (tutorRef != null) {
                    withContext(Dispatchers.Main) {
                        _tutorRef = tutorRef
                        loadDisponibilita() // Carica disponibilità solo se il tutor è stato trovato
                    }
                    true
                } else {
                    withContext(Dispatchers.Main) {
                        _message.value = "Tutor non trovato"
                    }
                    false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nel recupero del tutor: ${e.message}"
                }
                false
            }
        }
    }



    // Ordina una lista di fasce orarie in base all'orario di inizio
    fun ordinaFasceOrarie(fasceOrarie: List<Calendario>): List<Calendario> {
        return fasceOrarie.sortedBy { it.oraInizio }
    }

}
