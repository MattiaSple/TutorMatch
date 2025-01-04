package com.example.tutormatch.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tutormatch.util.FirebaseUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

class RicercaTutorViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _posizioneStudente = MutableLiveData<GeoPoint>()
    val posizioneStudente: LiveData<GeoPoint> get() = _posizioneStudente

    private val _richiestaServiziLocalizzazione = MutableLiveData<ResolvableApiException?>()
    val richiestaServiziLocalizzazione: LiveData<ResolvableApiException?> get() = _richiestaServiziLocalizzazione


    @SuppressLint("MissingPermission") //il controllo è fatto nel fragment
    fun getPosizioneStudente() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Ottieni la posizione corrente utilizzando getCurrentLocation con alta precisione
                val location: Location? = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                // Verifica se 'location' è null
                val posizione = if (location != null) {
                    GeoPoint(location.latitude, location.longitude)
                } else {
                    // Se la posizione non è disponibile, usa una posizione statica
                    GeoPoint(41.9028, 12.4964)  // Coordinate di Roma
                }
                // Posta la posizione nel LiveData
                _posizioneStudente.postValue(posizione)
            } catch (e: Exception) {
                // Gestisci eventuali errori impostando una posizione statica
                _posizioneStudente.postValue(GeoPoint(41.9028, 12.4964))  // Coordinate di Roma
            }
        }
    }


    fun checkAndRequestLocation() {
        // Avvia una coroutine nel thread IO per eseguire operazioni di lunga durata (come la richiesta di posizione).
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Crea una richiesta per ottenere la posizione con priorità alta (alta accuratezza).
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L) // Intervallo preferito tra aggiornamenti: 10 secondi.
                    .setWaitForAccurateLocation(true) // Richiede una posizione il più precisa possibile.
                    .setMinUpdateIntervalMillis(5000L) // Intervallo minimo tra aggiornamenti: 5 secondi.
                    .setMaxUpdateDelayMillis(20000L) // Ritardo massimo prima dell'aggiornamento: 20 secondi.
                    .build()

                // Configura una richiesta per verificare le impostazioni di localizzazione necessarie.
                val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest) // Aggiunge la richiesta di posizione creata sopra.

                // Ottieni un client per controllare le impostazioni di localizzazione.
                val settingsClient = LocationServices.getSettingsClient(getApplication<Application>())

                // Controlla se le impostazioni richieste per la posizione sono soddisfatte.
                val task = settingsClient.checkLocationSettings(builder.build())

                try {
                    // Utilizza il metodo `await()` per sospendere l'esecuzione fino a quando il controllo delle impostazioni è completato.
                    task.await()
                    // Se non viene generata un'eccezione, significa che le impostazioni di localizzazione richieste sono abilitate.
                    getPosizioneStudente()
                } catch (exception: ResolvableApiException) {
                    // Se i servizi di localizzazione non sono configurati correttamente:
                    // `ResolvableApiException` indica che è possibile risolvere il problema mostrando un dialogo di sistema per attivare i servizi.
                    _richiestaServiziLocalizzazione.postValue(exception) // Comunica al Fragment/Activity che deve gestire la richiesta.
                }
            } catch (e: Exception) {
                // Se si verifica un errore generico (es. impossibile accedere ai servizi di posizione o timeout):
                // Usa una posizione statica predefinita (es. coordinate di Roma).
                _posizioneStudente.postValue(GeoPoint(41.9028, 12.4964)) // Coordinate di fallback.
            }
        }
    }
    fun getPosizioneStatica(userId: String) {
        // Lancia una coroutine nel viewModelScope con Dispatcher.IO per operazioni di I/O
        viewModelScope.launch(Dispatchers.IO) {
            // Chiamata alla funzione sospesa
            val utente = FirebaseUtil.getDocumentRefById(userId)
            val geoPointFirebase = FirebaseUtil.getGeoPoint(utente)

            // Ritorno sul thread principale per aggiornare la UI
            withContext(Dispatchers.Main) {
                if (geoPointFirebase != null) {
                    val geoPointOsmdroid = GeoPoint(geoPointFirebase.latitude, geoPointFirebase.longitude)
                    _posizioneStudente.postValue(geoPointOsmdroid)
                } else {
                    val geoPointOsmdroid = GeoPoint(41.9028, 12.4964)  // Coordinata di default (Roma)
                    _posizioneStudente.postValue(geoPointOsmdroid)
                }
            }
        }
    }

}
