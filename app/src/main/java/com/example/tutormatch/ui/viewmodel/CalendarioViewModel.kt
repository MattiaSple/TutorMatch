package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Calendario
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class CalendarioViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val disponibilitaCollection = firestore.collection("disponibilita")

    // LiveData per le disponibilità
    private val _lista_disponibilita = MutableLiveData<List<Calendario>>()
    val lista_disponibilita: LiveData<List<Calendario>> get() = _lista_disponibilita

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private lateinit var _tutorRef: DocumentReference

    // Funzione per impostare il riferimento del tutor
    fun setTutorReference(tutorRef: DocumentReference) {
        _tutorRef = tutorRef
        caricaDisponibilita(tutorRef)
    }

    // Funzione per aggiungere nuove disponibilità di un'ora ciascuna
    fun aggiungiDisponibilita(data: String, oraInizio: String, oraFine: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dataInizio = dateFormat.parse(data)

        val nuovaDisponibilita = mutableListOf<Calendario>()
        var currentOraInizio = oraInizio

        while (currentOraInizio < oraFine) {
            val parts = currentOraInizio.split(":")
            val ore = parts[0].toInt()
            val minuti = parts[1].toInt()

            val calendar = Calendar.getInstance()
            calendar.time = dataInizio
            calendar.set(Calendar.HOUR_OF_DAY, ore)
            calendar.set(Calendar.MINUTE, minuti)
            calendar.add(Calendar.HOUR_OF_DAY, 1)

            val nextOraFine = String.format(Locale.getDefault(), "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            nuovaDisponibilita.add(Calendario(_tutorRef, dataInizio, currentOraInizio, nextOraFine))
            currentOraInizio = nextOraFine
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                nuovaDisponibilita.forEach {
                    disponibilitaCollection.add(it).await()
                }
                caricaDisponibilita(_tutorRef)
                _message.postValue("Disponibilità aggiunta con successo")
            } catch (e: Exception) {
                _message.postValue("Errore nell'aggiunta della disponibilità: ${e.message}")
            }
        }
    }

    // Funzione per caricare le disponibilità
    private fun caricaDisponibilita(tutorRef: DocumentReference) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = disponibilitaCollection.whereEqualTo("tutorRef", tutorRef).get().await()
                val loadedDisponibilita = querySnapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Calendario::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                _lista_disponibilita.postValue(loadedDisponibilita)
            } catch (e: Exception) {
                _message.postValue("Errore nel caricamento delle disponibilità: ${e.message}")
            }
        }
    }
}
