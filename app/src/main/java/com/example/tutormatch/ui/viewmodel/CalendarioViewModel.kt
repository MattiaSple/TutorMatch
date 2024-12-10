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

    private val _lista_disponibilita = MutableLiveData<List<Calendario>>()
    val lista_disponibilita: LiveData<List<Calendario>> get() = _lista_disponibilita

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    val data = MutableLiveData<String>()
    val oraInizio = MutableLiveData<String>()
    val oraFine = MutableLiveData<String>()
    val statoPren = MutableLiveData<Boolean>()

    private lateinit var _tutorRef: DocumentReference

    private var updateOrariInizioCallback: (() -> Unit)? = null

    fun setUpdateOrariInizioCallback(callback: () -> Unit) {
        updateOrariInizioCallback = callback
    }


    fun setTutorReference(tutorRef: String) {
        viewModelScope.launch {
            try {
                // Recupera il tutor dall'ID
                _tutorRef = FirebaseUtil.getDocumentRefById(tutorRef)

                // Se il tutor viene recuperato con successo, carica le disponibilità
                loadDisponibilita()
            } catch (e: Exception) {
                // Gestisci eccezioni o errori imprevisti
                _message.postValue("Errore nel recupero del tutor: ${e.message}")
            }
        }
    }

    fun loadDisponibilita() {
        viewModelScope.launch{
            try {
                val loadedDisponibilita = FirebaseUtil.loadDisponibilita(_tutorRef)
                withContext(Dispatchers.Main) {
                    _lista_disponibilita.value = loadedDisponibilita
                    updateOrariInizioCallback?.invoke()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nel caricamento delle disponibilità: ${e.message}"
                }
            }
        }
    }

    fun caricaDisponibilitaPerData(data: String, callback: (List<String>) -> Unit) {
        data.let {
            viewModelScope.launch{
                try {
                    val existingOrari = FirebaseUtil.caricaDisponibilitaPerData(_tutorRef, data)
                    withContext(Dispatchers.Main) {
                        callback(existingOrari)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        callback(emptyList())
                    }
                }
            }
        }
    }

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


    suspend fun salvaDisponibilita(): Boolean {
        val dataVal = data.value?.trim() ?: ""
        val oraInizioVal = oraInizio.value ?: ""
        val oraFineVal = oraFine.value ?: ""
        val statoPrenVal = statoPren.value ?: false

        if (dataVal.isBlank() || oraInizioVal.isBlank() || oraFineVal.isBlank()) {
            _message.value = "Data, Ora Inizio e Ora Fine sono necessari!"
            return false
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dataParsed = dateFormat.parse(dataVal)
        val inizioParsed = timeFormat.parse(oraInizioVal)
        val fineParsed = timeFormat.parse(oraFineVal)

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
            withContext(Dispatchers.IO) {
                FirebaseUtil.salvaDisponibilita(disponibilitaList)
            }
            withContext(Dispatchers.Main) {
                _message.value = "Disponibilità salvate con successo"
                loadDisponibilita()
            }
            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _message.value = "Errore nel salvataggio della disponibilità: ${e.message}"
            }
            false
        }
    }





    fun eliminaDisponibilita(calendario: Calendario) {
        if(calendario.statoPren)
        {
            _message.value = "Eliminare prima la prenotazione\nIn tal caso avvisa lo studente!"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = FirebaseUtil.eliminaDisponibilita(calendario)
                withContext(Dispatchers.Main) {
                    if (success) {
                        _message.value = "Disponibilità eliminata con successo"
                    } else {
                        _message.value = "Errore nell'eliminazione della disponibilità"
                    }
                }
                loadDisponibilita() // Ricarica le disponibilità dopo l'eliminazione
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nell'eliminazione della disponibilità: ${e.message}"
                }
            }
        }
    }


    fun getTutorDaAnnuncio(annuncioId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Chiamata a FirebaseUtil per ottenere il tutorRef
                val tutorRef = FirebaseUtil.getTutorDaAnnuncioF(annuncioId)
                withContext(Dispatchers.Main) {
                    if (tutorRef != null) {
                        _tutorRef = tutorRef
                        loadDisponibilita()
                    }else{
                        _message.value = "Tutor non trovato"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nel recupero del tutor: ${e.message}"
                }
            }
        }
    }



    fun ordinaFasceOrarie(fasceOrarie: List<Calendario>): List<Calendario> {
        return fasceOrarie.sortedBy { it.oraInizio }
    }

}
