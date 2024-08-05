package com.example.tutormatch.ui.viewmodel

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarioViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val disponibilitaCollection = firestore.collection("calendario")

    // LiveData per le disponibilità
    private val _lista_disponibilita = MutableLiveData<List<Calendario>>()
    val lista_disponibilita: LiveData<List<Calendario>> get() = _lista_disponibilita

    // LiveData per i messaggi di errore o stato
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // LiveData per i campi di input
    val data = MutableLiveData<String>()
    val oraInizio = MutableLiveData<String>()
    val oraFine = MutableLiveData<String>()
    val statoPren = MutableLiveData<Boolean>()

    private lateinit var _tutorRef: DocumentReference

    // Funzione per impostare il riferimento del tutor
    fun setTutorReference(tutorRef: DocumentReference) {
        _tutorRef = tutorRef
        cleanUpExpiredDisponibilita()
        loadDisponibilita() // Carica le disponibilità una volta che il riferimento è stato impostato
    }

    // Funzione per caricare le disponibilità da Firestore
    private fun loadDisponibilita() {
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
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nel caricamento delle disponibilità: ${e.message}"
                }
            }
        }
    }

    // Funzione per caricare le disponibilità per una data specifica
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

    // Funzione per salvare una nuova disponibilità su Firestore
    fun salvaDisponibilita(): Boolean {
        val dataVal = data.value?.trim() ?: ""
        val oraInizioVal = oraInizio.value ?: ""
        val oraFineVal = oraFine.value ?: ""
        val statoPrenVal = statoPren.value ?: false

        // Verifica che tutti i campi siano compilati
        if (dataVal.isBlank() || oraInizioVal.isBlank() || oraFineVal.isBlank()) {
            _message.value = "Data, Ora Inizio e Ora Fine sono necessari!"
            return false
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dataParsed = dateFormat.parse(dataVal)

        // Crea un nuovo oggetto Calendario
        val nuovaDisponibilita = Calendario(
            tutorRef = _tutorRef,
            data = dataParsed,
            oraInizio = oraInizioVal,
            oraFine = oraFineVal,
            stato_pren = statoPrenVal
        )

        // Inserisce la disponibilità in Firestore
        viewModelScope.launch(Dispatchers.IO) {
            try {
                disponibilitaCollection.add(nuovaDisponibilita).await()
                withContext(Dispatchers.Main) {
                    _message.value = "Disponibilità salvata con successo"
                }
                loadDisponibilita()  // Aggiorna la lista delle disponibilità
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nel salvataggio della disponibilità: ${e.message}"
                }
            }
        }
        return true
    }

    // Funzione per eliminare una disponibilità da Firestore
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
                loadDisponibilita()  // Aggiorna la lista delle disponibilità
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nell'eliminazione della disponibilità: ${e.message}"
                }
            }
        }
    }

    // Funzione per pulire le disponibilità scadute
    private fun cleanUpExpiredDisponibilita() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = Date()
                val querySnapshot = disponibilitaCollection.whereLessThan("data", now).get().await()
                for (document in querySnapshot.documents) {
                    val calendario = document.toObject(Calendario::class.java)
                    calendario?.let {
                        val oraFine = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it.oraFine)
                        if (it.data.before(now) || (it.data == now && oraFine.before(now))) {
                            disponibilitaCollection.document(document.id).delete().await()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CalendarioViewModel", "Errore durante la pulizia delle disponibilità scadute: ${e.message}")
            }
        }
    }
}
