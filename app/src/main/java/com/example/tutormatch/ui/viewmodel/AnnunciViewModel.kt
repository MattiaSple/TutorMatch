package com.example.tutormatch.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Annuncio
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AnnunciViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val annunciCollection = firestore.collection("annunci")

    // LiveData per gli annunci
    private val _annunci = MutableLiveData<List<Annuncio>>()
    val annunci: LiveData<List<Annuncio>> get() = _annunci

    // LiveData per i messaggi di errore o stato
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // LiveData per i campi di input
    val descrizione = MutableLiveData<String>()
    val materia = MutableLiveData<String>()
    val online = MutableLiveData<Boolean>()
    val presenza = MutableLiveData<Boolean>()
    val prezzo = MutableLiveData<String>() // Usato come String per legarlo all'EditText

    private lateinit var _tutorRef: DocumentReference

    // Funzione per impostare il riferimento del tutor
    fun setTutorReference(tutorRef: DocumentReference) {
        _tutorRef = tutorRef
        loadAnnunci() // Carica gli annunci una volta che il riferimento è stato impostato
    }

    // Funzione per caricare gli annunci da Firestore
    private fun loadAnnunci() {
        _tutorRef?.let { tutorRef ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val querySnapshot = annunciCollection.whereEqualTo("tutor", tutorRef).get().await()
                    val loadedAnnunci = querySnapshot.documents.mapNotNull { document ->
                        try {
                            document.toObject(Annuncio::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _annunci.postValue(loadedAnnunci)
                } catch (e: Exception) {
                    _message.postValue("Errore nel caricamento degli annunci: ${e.message}")
                }
            }
        }
    }



    // Funzione per salvare un nuovo annuncio su Firestore
    // Funzione per salvare un nuovo annuncio su Firestore
    fun salvaAnnuncio(): Boolean {
        val descrizioneVal = descrizione.value ?: ""
        val materiaVal = materia.value ?: ""
        val onlineVal = online.value ?: false
        val presenzaVal = presenza.value ?: false
        val prezzoVal = prezzo.value ?: ""


        // Verifica che tutti i campi siano compilati
        if (descrizioneVal.isBlank() || materiaVal.isBlank() || prezzoVal.isBlank()) {
            _message.value = "Tutti i campi devono essere compilati"
            return false
        }

        // Crea un nuovo oggetto Annuncio
        val nuovoAnnuncio = Annuncio(
            descrizione = descrizioneVal,
            materia = materiaVal,
            mod_on = onlineVal,
            mod_pres = presenzaVal,
            prezzo = prezzoVal,
            tutor = _tutorRef // Imposta il riferimento al tutor
        )

        // Inserisce l'annuncio in Firestore
        viewModelScope.launch(Dispatchers.IO) {
            try {
                annunciCollection.add(nuovoAnnuncio).await()
                loadAnnunci()  // Aggiorna la lista degli annunci
                _message.postValue("Annuncio salvato con successo")
            } catch (e: Exception) {
                _message.postValue("Errore nel salvataggio dell'annuncio: ${e.message}")
            }
        }
        return true
    }


    // Funzione per eliminare un annuncio da Firestore
    fun eliminaAnnuncio(annuncio: Annuncio) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = annunciCollection
                    .whereEqualTo("tutor", annuncio.tutor)
                    .whereEqualTo("materia", annuncio.materia)
                    .whereEqualTo("prezzo", annuncio.prezzo)
                    .whereEqualTo("descrizione", annuncio.descrizione)
                    .whereEqualTo("mod_on", annuncio.mod_on)
                    .whereEqualTo("mod_pres", annuncio.mod_pres)
                    .get().await()

                for (document in querySnapshot.documents) {
                    annunciCollection.document(document.id).delete().await()
                }
                loadAnnunci()  // Aggiorna la lista degli annunci
                _message.postValue("Annuncio eliminato con successo")
            } catch (e: Exception) {
                _message.postValue("Errore nell'eliminazione dell'annuncio: ${e.message}")
            }
        }
    }
}
