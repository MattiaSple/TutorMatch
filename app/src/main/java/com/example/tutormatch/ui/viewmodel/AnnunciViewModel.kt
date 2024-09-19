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
import com.example.tutormatch.util.FirebaseUtil
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

    // LiveData per gli annunci del singolo tutor
    private val _listaAnnunciTutor = MutableLiveData<List<Annuncio>>()
    val listaAnnunciTutor: LiveData<List<Annuncio>> get() = _listaAnnunciTutor

    // LiveData per tutti gli annunci
    private val _listaAnnunci = MutableLiveData<List<Annuncio>>()
    val listaAnnunci: LiveData<List<Annuncio>> get() = _listaAnnunci


    // LiveData per gli annunci filtrati
    private val _listaAnnunciFiltrati = MutableLiveData<List<Annuncio>>()
    val listaAnnunciFiltrati: LiveData<List<Annuncio>> get() = _listaAnnunciFiltrati


    // LiveData per i messaggi di errore o stato
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // LiveData per i campi di input del tutor
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

    // Funzione per caricare gli annunci da Firestore del tutor
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
        //Carico la lista corrente una sola volta
        getAllAnnunci()

        FirebaseUtil.osservaModificheAnnunciSuFirestore(
            onAnnunciUpdated = {
                getAllAnnunci()
            },
            onError = { exception ->
                // Gestisci eventuali errori
                _message.postValue("Errore durante l'ascolto degli annunci: ${exception.message}")
            }
        )
    }


    private fun getAllAnnunci()
    {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Ottieni tutti i documenti dalla collezione "annunciCollection"
                val querySnapshot = annunciCollection.get().await()

                // Estrai i dati dai documenti ottenuti
                val lista = querySnapshot.documents.mapNotNull { documentSnapshot ->
                    documentSnapshot.toObject(Annuncio::class.java)
                }
                withContext(Dispatchers.Main) {
                    _listaAnnunci.value = lista
                }

            }catch (e: Exception){
                _message.postValue("Errore nel caricamento degli annunci: ${e.message}")
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

                // Aggiungi l'annuncio alla collezione "annunci" e ottieni il riferimento al documento appena creato
                val documentReference = annunciCollection.add(nuovoAnnuncio).await()

                // Ottieni l'ID del documento generato automaticamente da Firebase
                val documentId = documentReference.id

                // Aggiorna l'oggetto Annuncio locale con l'ID del documento
                nuovoAnnuncio.id = documentId

                // (Opzionale) Aggiorna l'ID del documento anche su Firestore
                documentReference.update("id", documentId).await()

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


    // Funzione per eliminare un annuncio da Firestore e dalla listaAnnunci
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

    // Filtra gli annunci in base a più parametri
    fun filtraAnnunciMappa(
        filtroMateria: String,   // Parametro obbligatorio
        filtroBudget: Int,    // Budget massimo selezionato dalla SeekBar
        filtroModOn: Boolean,    // Modalità online selezionata?
        filtroModPres: Boolean   // Modalità in presenza selezionata?
    ) {
        // Controlla che ci siano annunci disponibili
        val tuttiGliAnnunci = _listaAnnunci.value ?: emptyList()

        // Filtra in base alla materia (sempre presente)
        var annunciFiltrati = tuttiGliAnnunci.filter { annuncio ->
            annuncio.materia == filtroMateria
        }

        // Filtra in base al budget, se specificato
        if (filtroBudget != 0) {
            annunciFiltrati = annunciFiltrati.filter { annuncio ->
                annuncio.prezzo.toInt() <= filtroBudget
            }
        }

        // Filtra in base alle modalità (online e/o in presenza)
        annunciFiltrati = annunciFiltrati.filter { annuncio ->
            (filtroModOn && annuncio.mod_on) || (filtroModPres && annuncio.mod_pres)
        }

        // Aggiorna il LiveData con la lista filtrata
        _listaAnnunciFiltrati.postValue(annunciFiltrati)
    }
}