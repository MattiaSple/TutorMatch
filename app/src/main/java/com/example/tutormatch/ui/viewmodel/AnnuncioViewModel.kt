package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnnuncioViewModel(application: Application) : AndroidViewModel(application) {

    var flagFiltro: Boolean = false // Flag per indicare se il filtro è attivo

    // LiveData per contenere i dati degli annunci
    private val _listaAnnunciTutor = MutableLiveData<List<Annuncio>>()
    val listaAnnunciTutor: LiveData<List<Annuncio>> get() = _listaAnnunciTutor

    val _listaAnnunci = MutableLiveData<List<Annuncio>>()
    val listaAnnunci: LiveData<List<Annuncio>> get() = _listaAnnunci

    private val _listaAnnunciFiltrati = MutableLiveData<List<Annuncio>>()
    val listaAnnunciFiltrati: LiveData<List<Annuncio>> get() = _listaAnnunciFiltrati

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    val materia = MutableLiveData<String>() // Materia selezionata per i filtri

    lateinit var _tutorRef: DocumentReference // Riferimento al tutor su Firestore

    // Imposta il riferimento al tutor e carica i relativi annunci
    fun setTutorReference(tutorRef: String) {
        _tutorRef = FirebaseUtil.getDocumentRefById(tutorRef)
        loadAnnunci()
    }

    // Carica gli annunci associati al tutor
    private fun loadAnnunci() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listaAnnunci = FirebaseUtil.getAnnunciDelTutor(_tutorRef)
                withContext(Dispatchers.Main) {
                    _listaAnnunciTutor.value = listaAnnunci
                }
            } catch (e: Exception) {
                _message.postValue("Errore nel caricamento dei tuoi annunci: ${e.message}")
            }
        }
    }

    // Aggiorna gli annunci in tempo reale ascoltando le modifiche su Firestore
    fun aggiornaListaAnnunciInTempoReale() {
        getAllAnnunci()

        FirebaseUtil.osservaModificheAnnunciSuFirestore(
            onAnnunciUpdated = {
                getAllAnnunci()
            },
            onError = { exception ->
                _message.value = "Errore durante l'ascolto degli annunci: ${exception.message}"
            }
        )
    }

    // Recupera tutti gli annunci dalla collezione Firestore
    private fun getAllAnnunci() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listaAnnunci = FirebaseUtil.getAllAnnunci()
                withContext(Dispatchers.Main) {
                    _listaAnnunci.value = listaAnnunci
                }
            } catch (e: Exception) {
                _message.postValue("Errore nel caricamento degli annunci: ${e.message}")
            }
        }
    }

    // Salva un nuovo annuncio su Firestore
    suspend fun salvaAnnuncio(
        materia: String,
        prezzo: String,
        descrizione: String,
        online: Boolean,
        presenza: Boolean
    ) {
        if (materia.isBlank() || prezzo.isBlank() || (!online && !presenza)) {
            withContext(Dispatchers.Main) {
                _message.value = "Prezzo e Modalità sono obbligatori!"
            }
            return
        }

        try {
            withContext(Dispatchers.IO) {
                val esiste = FirebaseUtil.verificaAnnuncioEsistente(
                    materia, prezzo, descrizione, online, presenza, _tutorRef
                )
                if (esiste) {
                    withContext(Dispatchers.Main) {
                        _message.value = "L'annuncio è già esistente!"
                    }
                    return@withContext
                }

                val geoPoint = FirebaseUtil.getGeoPoint(_tutorRef)
                if (geoPoint != null) {
                    FirebaseUtil.salvaAnnuncioConGeoPoint(
                        geoPoint, materia, prezzo, descrizione, online, presenza, _tutorRef
                    )
                } else {
                    withContext(Dispatchers.Main) {
                        _message.value = "Errore nella geocodifica dell'indirizzo"
                    }
                    return@withContext
                }

                withContext(Dispatchers.Main) {
                    _message.value = "Annuncio salvato con successo"
                    loadAnnunci()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _message.value = "Errore nel caricamento dei dati dell'utente: ${e.message}"
            }
        }
    }

    // Elimina un annuncio esistente
    fun eliminaAnnuncio(annuncio: Annuncio) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val successo = FirebaseUtil.eliminaAnnuncio(annuncio)
                withContext(Dispatchers.Main) {
                    if (successo) {
                        loadAnnunci()
                        _message.value = "Annuncio eliminato con successo"
                    } else {
                        _message.value = "Prima elimina le prenotazioni associate a questo annuncio!"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _message.value = "Errore nell'eliminazione dell'annuncio: ${e.message}"
                }
            }
        }
    }

    // Filtra gli annunci in base ai criteri forniti
    fun filtraAnnunciMappa(
        filtroMateria: String,
        filtroBudget: Int,
        filtroModOn: Boolean,
        filtroModPres: Boolean
    ) {
        val tuttiGliAnnunci = _listaAnnunci.value ?: emptyList()

        var annunciFiltrati = tuttiGliAnnunci.filter { annuncio ->
            annuncio.materia == filtroMateria
        }

        if (filtroBudget != 0) {
            annunciFiltrati = annunciFiltrati.filter { annuncio ->
                annuncio.prezzo.toInt() <= filtroBudget
            }
        }

        annunciFiltrati = annunciFiltrati.filter { annuncio ->
            (filtroModOn && annuncio.mod_on) || (filtroModPres && annuncio.mod_pres)
        }

        _listaAnnunciFiltrati.postValue(annunciFiltrati)
    }

    // Svuota la lista degli annunci filtrati
    fun svuotaListaAnnunciFiltrati() {
        _listaAnnunciFiltrati.postValue(emptyList())
    }
}
