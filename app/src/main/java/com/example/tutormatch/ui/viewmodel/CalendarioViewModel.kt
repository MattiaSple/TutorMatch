package com.example.tutormatch.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarioViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val disponibilitaCollection = firestore.collection("calendario")

    private val _lista_disponibilita = MutableLiveData<List<Calendario>>()
    val lista_disponibilita: LiveData<List<Calendario>> get() = _lista_disponibilita

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    val data = MutableLiveData<String>()
    val oraInizio = MutableLiveData<String>()
    val oraFine = MutableLiveData<String>()
    val statoPren = MutableLiveData<Boolean>()

    lateinit var tutorId: String

    private lateinit var _tutorRef: DocumentReference

    private var updateOrariInizioCallback: (() -> Unit)? = null

    fun setUpdateOrariInizioCallback(callback: () -> Unit) {
        updateOrariInizioCallback = callback
    }


    fun setTutorReference(tutorRef: DocumentReference) {
        _tutorRef = tutorRef
        loadDisponibilita()
    }

    fun loadDisponibilita() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = disponibilitaCollection.whereEqualTo("tutorRef", _tutorRef).get().await()
                val loadedDisponibilita = querySnapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Calendario::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
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

    fun caricaDisponibilitaPerData(data: String?, callback: (List<String>) -> Unit) {
        data?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dataParsed = dateFormat.parse(data)
                    val querySnapshot = disponibilitaCollection
                        .whereEqualTo("tutorRef", _tutorRef)
                        .whereEqualTo("data", dataParsed)
                        .get().await()
                    val existingOrari = querySnapshot.documents.mapNotNull { document ->
                        try {
                            val calendario = document.toObject(Calendario::class.java)
                            calendario?.oraInizio
                        } catch (e: Exception) {
                            null
                        }
                    }
                    withContext(Dispatchers.Main) {
                        callback(existingOrari)
                    }
                } catch (e: Exception) {
                    Log.e("CalendarioViewModel", "Errore nel caricamento delle disponibilità per data: ${e.message}")
                    withContext(Dispatchers.Main) {
                        callback(emptyList())
                    }
                }
            }
        } ?: callback(emptyList())
    }

    fun generateOrari(selectedDate: String?, existingOrari: List<String>): List<String> {
        val orari = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val oggi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var oreRimanenti = 24

        if (selectedDate == oggi) {
            val currentTime = Calendar.getInstance()
            currentTime.add(Calendar.MINUTE, 60 - (currentTime.get(Calendar.MINUTE) % 60))
            calendar.time = currentTime.time
            oreRimanenti = 24 - currentTime.get(Calendar.HOUR_OF_DAY)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        }

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

    fun salvaDisponibilita(): Boolean {
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

        if (fineParsed.before(inizioParsed)) {
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

            disponibilitaList.add(Calendario(
                tutorRef = _tutorRef,
                data = dataParsed,
                oraInizio = oraInizioStr,
                oraFine = oraFineStr,
                statoPren = statoPrenVal
            ))
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                for (disponibilita in disponibilitaList) {
                    val existingSnapshot = disponibilitaCollection
                        .whereEqualTo("tutorRef", disponibilita.tutorRef)
                        .whereEqualTo("data", disponibilita.data)
                        .whereEqualTo("oraInizio", disponibilita.oraInizio)
                        .get()
                        .await()

                    if (existingSnapshot.isEmpty) {
                        disponibilitaCollection.add(disponibilita).await()
                    }
                }
                withContext(Dispatchers.Main) {
                    _message.value = "Disponibilità salvate con successo"
                }
                loadDisponibilita()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nel salvataggio della disponibilità: ${e.message}"
                }
            }
        }
        return true
    }

    fun eliminaDisponibilita(calendario: Calendario) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = disponibilitaCollection
                    .whereEqualTo("tutorRef", calendario.tutorRef)
                    .whereEqualTo("data", calendario.data)
                    .whereEqualTo("oraInizio", calendario.oraInizio)
                    .whereEqualTo("oraFine", calendario.oraFine)
                    .get().await()
                for (document in querySnapshot.documents) {
                    disponibilitaCollection.document(document.id).delete().await()
                }
                withContext(Dispatchers.Main) {
                    _message.value = "Disponibilità eliminata con successo"
                }
                loadDisponibilita()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nell'eliminazione della disponibilità: ${e.message}"
                }
            }
        }
    }

    fun eliminaFasceScadutePerTutor(onComplete: () -> Unit) {
        FirebaseUtil.eliminaFasceOrarieScadutePerTutor(_tutorRef) { successo, errore ->
            if (successo) {
                // Se l'operazione è completata con successo, chiama il callback
                onComplete()
            } else {
                // Gestisci l'errore
                Log.e("CalendarioViewModel", "Errore eliminazione fasce scadute: $errore")
                onComplete() // Anche in caso di errore, chiama il callback per proseguire
            }
        }
    }


    fun getTutorDaAnnuncio(annuncioId: String, callback: (DocumentReference?) -> Unit) {
        FirebaseUtil.getTutorDaAnnuncioF(
            annuncioId,
            onSuccess = { tutorPrelevatoDaAnnuncio ->
                callback(tutorPrelevatoDaAnnuncio)  // Restituisci il tutorRef tramite il callback
            },
            onFailure = { exception ->
                _message.value = "Errore nel recupero del tutor: ${exception.message}"
                callback(null)  // Restituisci null in caso di errore
            }
        )
    }
    fun ordinaFasceOrarie(fasceOrarie: List<Calendario>): List<Calendario> {
        return fasceOrarie.sortedBy { it.oraInizio }
    }
}
