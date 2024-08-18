package com.example.tutormatch.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint

class RicercaTutorViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _posizioneStudente = MutableLiveData<GeoPoint>()
    val posizioneStudente: LiveData<GeoPoint> get() = _posizioneStudente

    private val _richiestaServiziLocalizzazione = MutableLiveData<ResolvableApiException?>()
    val richiestaServiziLocalizzazione: LiveData<ResolvableApiException?> get() = _richiestaServiziLocalizzazione


    @SuppressLint("MissingPermission")
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


//    @SuppressLint("MissingPermission")
//    fun checkAndRequestLocation() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
//                    .setWaitForAccurateLocation(true)
//                    .setMinUpdateIntervalMillis(5000L)
//                    .setMaxUpdateDelayMillis(20000L)
//                    .build()
//
//                val builder = LocationSettingsRequest.Builder()
//                    .addLocationRequest(locationRequest)
//
//
//                val settingsClient = LocationServices.getSettingsClient(getApplication<Application>())
//                val task = settingsClient.checkLocationSettings(builder.build())
//
//                try {
//                    val response = task.await()
//                    // Se i servizi di localizzazione sono abilitati, ottieni la posizione
//                    getPosizioneStudente()
//                } catch (exception: ResolvableApiException) {
//                    // I servizi di localizzazione non sono abilitati, richiedi l'attivazione
//                    _richiestaServiziLocalizzazione.postValue(exception)
//                }
//            } catch (e: Exception) {
//                _posizioneStudente.postValue(GeoPoint(41.9028, 12.4964))  // Coordinate di Roma
//            }
//        }
//    }



}
