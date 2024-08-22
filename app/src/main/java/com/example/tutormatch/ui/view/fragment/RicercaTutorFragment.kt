package com.example.tutormatch.ui.view.fragment

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tutormatch.R
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.databinding.FragmentRicercaTutorBinding
import com.example.tutormatch.ui.viewmodel.AnnunciViewModel
import com.example.tutormatch.ui.viewmodel.RicercaTutorViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class RicercaTutorFragment : Fragment() {

    private var _binding: FragmentRicercaTutorBinding? = null
    private val binding get() = _binding!!
    private lateinit var ricercaTutorViewModel: RicercaTutorViewModel
    private lateinit var annunciViewModel: AnnunciViewModel
    private lateinit var mapView: MapView
    private lateinit var locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>

    // Mappa per tenere traccia dei marker sulla mappa
    private val markerMap = mutableMapOf<String, Marker>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            ricercaTutorViewModel.getPosizioneStudente()
        } else {
            // Gestisci il caso in cui il permesso non viene concesso
            setPosizioneStatica()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RicercaTutorFragment", "onCreate")
        // Inizializza il launcher per gestire il risultato delle impostazioni di localizzazione
        locationSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // L'utente ha attivato i servizi di localizzazione, procedi
                ricercaTutorViewModel.getPosizioneStudente()
            } else {
                // L'utente ha rifiutato di attivare i servizi di localizzazione
                setPosizioneStatica()
            }
        }

        // Configura il User-Agent per osmdroid
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRicercaTutorBinding.inflate(inflater, container, false)
        Log.d("RicercaTutorFragment", "onCreateView")
        // Inizializza i ViewModel
        ricercaTutorViewModel = ViewModelProvider(this)[RicercaTutorViewModel::class.java]
        annunciViewModel = ViewModelProvider(this)[AnnunciViewModel::class.java]

        // Configura la mappa
        mapView = binding.mapView
        mapView.controller.setZoom(5.0)
        mapView.setMultiTouchControls(true)

        // Osserva i cambiamenti nella posizione dello studente
        ricercaTutorViewModel.posizioneStudente.observe(viewLifecycleOwner, Observer { posizione ->
            posizione?.let {
                val studentMarker = Marker(mapView).apply {
                    position = it
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Io"
                    icon = resources.getDrawable(R.drawable.marker_stud, null)
                }
                mapView.overlays.add(studentMarker)
                // Centra la mappa sulla posizione del marker
                mapView.controller.animateTo(it)
                mapView.controller.setZoom(15.0)
                mapView.invalidate()  // Aggiorna la mappa
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("RicercaTutorFragment", "onViewCreated")

        // Osserva la richiesta di attivazione dei servizi di localizzazione
        ricercaTutorViewModel.richiestaServiziLocalizzazione.observe(viewLifecycleOwner, Observer { resolvable ->
            resolvable?.let {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(it.resolution).build()
                    locationSettingsLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    setPosizioneStatica()
                }
            }
        })

        // Controlla i permessi e ottieni la posizione
        if (checkLocationPermission()) {
            ricercaTutorViewModel.checkAndRequestLocation()
        } else {
            requestLocationPermission()
        }

        // Attiva l'osservazione in tempo reale
        annunciViewModel.observeAnnunciInTempoReale()


        // Osserva la lista degli annunci e aggiorna la mappa
        annunciViewModel.listaAnnunci.observe(viewLifecycleOwner, Observer { lista ->
            aggiornaMappa(lista)
        })

        // Aggiorna immediatamente la mappa se ci sono già annunci caricati
        annunciViewModel.listaAnnunci.value?.let { lista ->
            aggiornaMappa(lista)
        }
    }

    private fun aggiornaMappa(listaAnnunci: List<Annuncio>) {
        try {
            // Rimuovi i marker che non sono più presenti nella lista
            val annunciId = listaAnnunci.map { annuncio -> annuncio.id }.toSet()
            val markerIdsToRemove = markerMap.keys - annunciId
            for (id in markerIdsToRemove) {
                markerMap[id]?.let { marker ->
                    mapView.overlays.remove(marker)  // Rimuovi il marker dalla mappa
                }
                markerMap.remove(id)  // Rimuovi il marker dalla mappa dei marker
            }

            // Aggiungi nuovi marker e aggiorna quelli esistenti
            for (annuncio in listaAnnunci) {
                if (!markerMap.containsKey(annuncio.id)) {
                    // Aggiungi un nuovo marker se non esiste già
                    val osmdroidGeoPoint = GeoPoint(annuncio.posizione.latitude, annuncio.posizione.longitude)
                    val marker = Marker(mapView).apply {
                        position = osmdroidGeoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = annuncio.materia
                        snippet = annuncio.descrizione
                        icon = resources.getDrawable(R.drawable.marker_annuncio, null)
                    }
                    mapView.overlays.add(marker)
                    markerMap[annuncio.id] = marker  // Aggiungi il marker alla mappa dei marker
                } else {
                    // Aggiorna la posizione del marker esistente se necessario
                    val marker = markerMap[annuncio.id]
                    marker?.position = GeoPoint(annuncio.posizione.latitude, annuncio.posizione.longitude)
                }
            }

            // Aggiorna la mappa per visualizzare le modifiche
            mapView.invalidate()

        } catch (e: Exception) {
            Log.e("RicercaTutorFragment", "Errore durante l'aggiornamento degli annunci sulla mappa", e)
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Metodo per controllare se i permessi di localizzazione sono stati concessi
    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Metodo per centrare la mappa su una posizione statica se non è possibile ottenere la posizione dell'utente
    private fun setPosizioneStatica() {
        val romeLocation = GeoPoint(41.9028, 12.4964)  // Coordinate di Roma
        mapView.controller.setCenter(romeLocation)
        mapView.controller.setZoom(10.0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach() // per rilasciare tutte le risorse legate alla mappa e prevenire potenziali memory leaks.
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
}
