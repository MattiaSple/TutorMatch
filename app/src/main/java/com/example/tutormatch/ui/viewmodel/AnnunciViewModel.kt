package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.network.LocationResponse
import com.google.firebase.firestore.DocumentReference
import com.example.tutormatch.network.RetrofitInstance
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


    // LiveData per gli annunci
    private val _lista_annunci = MutableLiveData<List<Annuncio>>()
    val lista_annunci: LiveData<List<Annuncio>> get() = _lista_annunci

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
        _tutorRef.let { tutorRef ->
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
                    _lista_annunci.postValue(loadedAnnunci)
                } catch (e: Exception) {
                    _message.postValue("Errore nel caricamento degli annunci: ${e.message}")
                }
            }
        }
    }


    // Funzione per salvare un nuovo annuncio su Firestore
    fun salvaAnnuncio(userId: String): Boolean {

        // Verifica che tutti i campi siano compilati
        if (materia.value!!.isBlank() || prezzo.value!!.isBlank()) {
            return false
        }

        // Recupera i dati dell'utente e salva l'annuncio
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = utentiCollection.document(userId).get().await()
                val utente = documentSnapshot.toObject(Utente::class.java)
                utente?.let {
                    val indirizzoCompleto = "${it.via}, ${it.cap}, ${it.residenza}"
                    val indirizzoSenzaVia = "${it.cap}, ${it.residenza}"

                    // Primo tentativo: Ottieni il GeoPoint per l'indirizzo completo
                    getGeoPoint(indirizzoCompleto) { geoPoint ->
                        if (geoPoint != null) {
                            salvaAnnuncioConGeoPoint(geoPoint)
                        } else {
                            // Secondo tentativo: Ottieni il GeoPoint senza la via
                            getGeoPoint(indirizzoSenzaVia) { geoPointSenzaVia ->
                                if (geoPointSenzaVia != null) {
                                    salvaAnnuncioConGeoPoint(geoPointSenzaVia)
                                } else {
                                    // Entrambi i tentativi falliti
                                    _message.postValue("Errore nella geocodifica dell'indirizzo")
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
        return true
    }

    // Funzione di supporto per salvare l'annuncio con il GeoPoint ottenuto
    private fun salvaAnnuncioConGeoPoint(geoPoint: GeoPoint) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nuovoAnnuncio = Annuncio(
                    descrizione = descrizione.value?.trim()?.replace("\\s+".toRegex(), " ") ?: "",
                    materia = materia.value!!,
                    mod_on = online.value ?: false,
                    mod_pres = presenza.value ?: false,
                    posizione = geoPoint,
                    prezzo = prezzo.value!!,
                    tutor = _tutorRef
                )

                annunciCollection.add(nuovoAnnuncio).await()
                loadAnnunci()  // Aggiorna la lista degli annunci
                _message.postValue("Annuncio salvato con successo")
            } catch (e: Exception) {
                _message.postValue("Errore nel salvataggio dell'annuncio: ${e.message}")
            }
        }
    }

    private fun getGeoPoint(address: String, callback: (GeoPoint?) -> Unit) {
        // La chiamata è già asincrona, quindi non dovrebbe bloccare il thread principale.
        val call = RetrofitInstance.api.getLocation(address)
        call.enqueue(object : Callback<List<LocationResponse>> {
            override fun onResponse(
                call: Call<List<LocationResponse>>,
                response: Response<List<LocationResponse>>
            ) {
                viewModelScope.launch(Dispatchers.IO) {
                    if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                        val location = response.body()!![0]
                        val geoPoint = GeoPoint(location.lat.toDouble(), location.lon.toDouble())

                        // Torna al thread principale per il callback
                        withContext(Dispatchers.Main) {
                            callback(geoPoint)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            callback(null)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<LocationResponse>>, t: Throwable) {
                callback(null)
            }
        })
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
