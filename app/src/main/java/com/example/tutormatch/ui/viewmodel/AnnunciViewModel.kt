package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.network.RetrofitInstance
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AnnunciViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val annunciCollection = firestore.collection("annunci")
    private val utentiCollection = firestore.collection("utenti")

    var flagFiltro: Boolean = false

    private val _listaAnnunciTutor = MutableLiveData<List<Annuncio>>()
    val listaAnnunciTutor: LiveData<List<Annuncio>> get() = _listaAnnunciTutor

    private val _listaAnnunci = MutableLiveData<List<Annuncio>>()
    val listaAnnunci: LiveData<List<Annuncio>> get() = _listaAnnunci

    private val _listaAnnunciFiltrati = MutableLiveData<List<Annuncio>>()
    val listaAnnunciFiltrati: LiveData<List<Annuncio>> get() = _listaAnnunciFiltrati

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    val materia = MutableLiveData<String>()

    private lateinit var _tutorRef: DocumentReference

    fun setTutorReference(tutorRef: String) {
        _tutorRef = FirebaseUtil.getDocumentRefById(tutorRef)
        loadAnnunci()
    }

    private fun loadAnnunci() {
        _tutorRef.let { tutorRef ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val querySnapshot = annunciCollection.whereEqualTo("tutor", tutorRef).get().await()
                    val lista = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(Annuncio::class.java)
                    }
                    _listaAnnunciTutor.postValue(lista)
                } catch (e: Exception) {
                    _message.postValue("Errore nel caricamento dei tuoi annunci: ${e.message}")
                }
            }
        }
    }

    fun aggiornaListaAnnunciInTempoReale() {
        getAllAnnunci()

        FirebaseUtil.osservaModificheAnnunciSuFirestore(
            onAnnunciUpdated = {
                getAllAnnunci()
            },
            onError = { exception ->
                _message.postValue("Errore durante l'ascolto degli annunci: ${exception.message}")
            }
        )
    }

    private fun getAllAnnunci() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = annunciCollection.get().await()
                val lista = querySnapshot.documents.mapNotNull { documentSnapshot ->
                    documentSnapshot.toObject(Annuncio::class.java)
                }
                withContext(Dispatchers.Main) {
                    _listaAnnunci.value = lista
                }
            } catch (e: Exception) {
                _message.postValue("Errore nel caricamento degli annunci: ${e.message}")
            }
        }
    }

    fun salvaAnnuncio(
        userId: String,
        materia: String,
        prezzo: String,
        descrizione: String,
        online: Boolean,
        presenza: Boolean
    ) {
        if (materia.isBlank() || prezzo.isBlank() || (!online && !presenza)) {
            _message.value = "Prezzo e Modalità sono obbligatori!"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Recupera i dati dell'utente
                val documentSnapshot = utentiCollection.document(userId).get().await()
                val utente = documentSnapshot.toObject(Utente::class.java)

                utente?.let {
                    val indirizzoCompleto = "${it.via}, ${it.cap}, ${it.residenza}"
                    val indirizzoSenzaVia = "${it.cap}, ${it.residenza}"

                    // Verifica se l'annuncio esiste già
                    val esiste = FirebaseUtil.verificaAnnuncioEsistente(
                        materia, prezzo, descrizione, online, presenza, _tutorRef
                    )

                    if (esiste) {
                        _message.postValue("L'annuncio è già esistente!")
                    } else {
                        // Prova a ottenere il GeoPoint per l'indirizzo completo
                        val geoPoint = FirebaseUtil.getGeoPoint(indirizzoCompleto)

                        if (geoPoint != null) {
                            // Salva l'annuncio con il GeoPoint
                            val success = FirebaseUtil.salvaAnnuncioConGeoPoint(
                                geoPoint, materia, prezzo, descrizione, online, presenza, _tutorRef
                            )
                            if (success) {
                                _message.postValue("Annuncio salvato con successo")
                                loadAnnunci()
                            } else {
                                _message.postValue("Errore nel salvataggio dell'annuncio")
                            }
                        } else {
                            // Prova con l'indirizzo senza via
                            val geoPointSenzaVia = FirebaseUtil.getGeoPoint(indirizzoSenzaVia)

                            if (geoPointSenzaVia != null) {
                                val success = FirebaseUtil.salvaAnnuncioConGeoPoint(
                                    geoPointSenzaVia, materia, prezzo, descrizione, online, presenza, _tutorRef
                                )
                                if (success) {
                                    _message.postValue("Annuncio salvato con successo")
                                    loadAnnunci()
                                } else {
                                    _message.postValue("Errore nel salvataggio dell'annuncio")
                                }
                            } else {
                                _message.postValue("Errore nella geocodifica dell'indirizzo")
                            }
                        }
                    }
                } ?: run {
                    _message.postValue("Errore nel caricamento dei dati dell'utente")
                }
            } catch (e: Exception) {
                _message.postValue("Errore nel caricamento dei dati dell'utente: ${e.message}")
            }
        }
    }


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
                loadAnnunci()
                _message.postValue("Annuncio eliminato con successo")
            } catch (e: Exception) {
                _message.postValue("Errore nell'eliminazione dell'annuncio: ${e.message}")
            }
        }
    }

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

    fun svuotaListaAnnunciFiltrati() {
        _listaAnnunciFiltrati.postValue(emptyList())
    }
}
