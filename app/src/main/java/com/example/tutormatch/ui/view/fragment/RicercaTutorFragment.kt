package com.example.tutormatch.ui.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tutormatch.R
import com.example.tutormatch.databinding.FragmentRicercaTutorBinding
import com.example.tutormatch.ui.viewmodel.RicercaTutorViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class RicercaTutorFragment : Fragment() {

    private lateinit var _binding: FragmentRicercaTutorBinding
    private val binding get() = _binding
    private lateinit var viewModel: RicercaTutorViewModel
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userMarker: Marker? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    // Utilizza ActivityResultLauncher per gestire i permessi
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permesso concesso
            getUserLocation()
        } else {
            // Permesso negato, imposta una posizione statica (ad es. Roma)
            setStaticLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRicercaTutorBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[RicercaTutorViewModel::class.java]

        // Configura la mappa OSMDroid
        mapView = binding.mapView
        Configuration.getInstance().load(
            context,
            context?.let { androidx.preference.PreferenceManager.getDefaultSharedPreferences(it) }
        )
        mapView.setMultiTouchControls(true)

        // Inizializza il client per ottenere la posizione
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Carica i dati iniziali degli annunci
        viewModel.loadAnnunci()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ora che la vista è stata creata, possiamo interagire con la mappa
        viewModel.annunci.observe(viewLifecycleOwner, Observer { annunci ->
            mapView.overlays.clear()  // Rimuovi tutti gli overlay esistenti
            for (annuncio in annunci) {
                val position = GeoPoint(annuncio.posizione.latitude, annuncio.posizione.longitude)
                val marker = Marker(mapView)
                marker.position = position
                marker.title = annuncio.descrizione
                mapView.overlays.add(marker)
            }

            // Aggiungi il marker dell'utente se esiste
            userMarker?.let {
                if (it.position != null) {
                    mapView.overlays.add(it)
                }
            }

            mapView.invalidate()  // Aggiorna la mappa
        })

        // Controlla periodicamente per nuovi annunci
        checkForNewAnnouncementsPeriodically()

        // Avvia il processo di ottenimento della posizione
        if (checkLocationPermission()) {
            getUserLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getUserLocation() {
        if (checkLocationPermission()) {
            // Ottieni l'ultima posizione conosciuta del dispositivo
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    mapView.controller.setCenter(userLocation)
                    mapView.controller.setZoom(15.0)

                    userMarker = Marker(mapView).apply {
                        position = userLocation
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "La tua posizione"
                        icon = resources.getDrawable(R.drawable.marker_stud, null)
                    }
                    mapView.overlays.add(userMarker)
                    mapView.invalidate()
                } ?: run {
                    // Se la posizione non è disponibile, usa la posizione statica
                    setStaticLocation()
                }
            }.addOnFailureListener {
                // Se il recupero della posizione fallisce, usa la posizione statica
                setStaticLocation()
            }
        } else {
            // Se non ci sono i permessi, richiedili
            requestLocationPermission()
        }
    }

    private fun setStaticLocation() {
        val romeLocation = GeoPoint(41.9028, 12.4964) // Roma, Italia
        mapView.controller.setCenter(romeLocation)
        mapView.controller.setZoom(10.0)

        userMarker = Marker(mapView).apply {
            position = romeLocation
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Posizione predefinita (Roma)"
            icon = resources.getDrawable(R.drawable.marker_stud, null)
        }
        mapView.overlays.add(userMarker)
        mapView.invalidate()
    }

    private fun checkForNewAnnouncementsPeriodically() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                viewModel.checkForNewAnnunci()
                handler.postDelayed(this, 60000) // Controlla ogni minuto
            }
        }
        handler.post(runnable)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        // Verifica se i permessi sono stati concessi e ottieni la posizione dell'utente
        if (checkLocationPermission()) {
            getUserLocation()
        } else {
            setStaticLocation() // Se i permessi non sono stati concessi, usa la posizione statica
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()
    }
}
