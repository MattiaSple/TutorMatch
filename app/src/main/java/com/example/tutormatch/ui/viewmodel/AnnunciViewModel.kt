package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.network.LocationResponse
import com.example.tutormatch.network.RetrofitInstance
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    fun setTutorReference(tutorRef: DocumentReference) {
        _tutorRef = tutorRef
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
                val documentSnapshot = utentiCollection.document(userId).get().await()
                val utente = documentSnapshot.toObject(Utente::class.java)
                utente?.let {
                    val indirizzoCompleto = "${it.via}, ${it.cap}, ${it.residenza}"
                    val indirizzoSenzaVia = "${it.cap}, ${it.residenza}"

                    verificaAnnuncioEsistente(materia, prezzo, descrizione, online, presenza) { esiste ->
                        if (esiste) {
                            _message.postValue("L'annuncio è già esistente!")
                        } else {
                            getGeoPoint(indirizzoCompleto) { geoPoint ->
                                if (geoPoint != null) {
                                    salvaAnnuncioConGeoPoint(geoPoint, materia, prezzo, descrizione, online, presenza)
                                } else {
                                    getGeoPoint(indirizzoSenzaVia) { geoPointSenzaVia ->
                                        if (geoPointSenzaVia != null) {
                                            salvaAnnuncioConGeoPoint(geoPointSenzaVia, materia, prezzo, descrizione, online, presenza)
                                        } else {
                                            _message.postValue("Errore nella geocodifica dell'indirizzo")
                                        }
                                    }
                                }
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

    private fun verificaAnnuncioEsistente(
        materia: String,
        prezzo: String,
        descrizione: String,
        online: Boolean,
        presenza: Boolean,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = annunciCollection
                    .whereEqualTo("materia", materia)
                    .whereEqualTo("prezzo", prezzo)
                    .whereEqualTo("descrizione", descrizione)
                    .whereEqualTo("mod_on", online)
                    .whereEqualTo("mod_pres", presenza)
                    .whereEqualTo("tutor", _tutorRef)
                    .get().await()

                callback(querySnapshot.documents.isNotEmpty())
            } catch (e: Exception) {
                _message.postValue("Errore nella verifica dell'annuncio: ${e.message}")
                callback(false)
            }
        }
    }

    private fun salvaAnnuncioConGeoPoint(
        geoPoint: GeoPoint,
        materia: String,
        prezzo: String,
        descrizione: String,
        online: Boolean,
        presenza: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nuovoAnnuncio = Annuncio(
                    descrizione = descrizione.trim().replace("\\s+".toRegex(), " "),
                    materia = materia,
                    mod_on = online,
                    mod_pres = presenza,
                    posizione = geoPoint,
                    prezzo = prezzo,
                    tutor = _tutorRef
                )

                val documentReference = annunciCollection.add(nuovoAnnuncio).await()
                val documentId = documentReference.id
                nuovoAnnuncio.id = documentId
                documentReference.update("id", documentId).await()

                loadAnnunci()
                _message.postValue("Annuncio salvato con successo")
            } catch (e: Exception) {
                _message.postValue("Errore nel salvataggio dell'annuncio: ${e.message}")
            }
        }
    }

    private fun getGeoPoint(address: String, callback: (GeoPoint?) -> Unit) {
        val call = RetrofitInstance.api.getLocation(address)
        call.enqueue(object : Callback<List<LocationResponse>> {
            override fun onResponse(call: Call<List<LocationResponse>>, response: Response<List<LocationResponse>>) {
                viewModelScope.launch(Dispatchers.IO) {
                    if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                        val location = response.body()!![0]
                        val geoPoint = GeoPoint(location.lat.toDouble(), location.lon.toDouble())
                        withContext(Dispatchers.Main) { callback(geoPoint) }
                    } else {
                        withContext(Dispatchers.Main) { callback(null) }
                    }
                }
            }
            override fun onFailure(call: Call<List<LocationResponse>>, t: Throwable) {
                callback(null)
            }
        })
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
