package com.example.tutormatch.ui.view.fragment

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.tutormatch.ui.viewmodel.AnnunciViewModel
import com.example.tutormatch.ui.viewmodel.RicercaTutorViewModel
import com.google.android.gms.location.FusedLocationProviderClient
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient


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

        // Configura il User-Agent per osmdroid
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRicercaTutorBinding.inflate(inflater, container, false)

        // Inizializza i ViewModel
        ricercaTutorViewModel = ViewModelProvider(this)[RicercaTutorViewModel::class.java]
        annunciViewModel = ViewModelProvider(this)[AnnunciViewModel::class.java]

        // Configura la mappa
        mapView = binding.mapView
        mapView.setMultiTouchControls(true)

        // Osserva i cambiamenti nella posizione dello studente
        ricercaTutorViewModel.posizioneStudente.observe(viewLifecycleOwner, Observer { posizione ->
            posizione?.let {
                mapView.controller.setCenter(it)
                mapView.controller.setZoom(15.0)

                val studentMarker = Marker(mapView).apply {
                    position = it
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Sei qui"
                    icon = resources.getDrawable(R.drawable.marker_stud, null)
                }
                mapView.overlays.add(studentMarker)
                mapView.invalidate()
            }
        })




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        // Osserva la richiesta di attivazione dei servizi di localizzazione
//        ricercaTutorViewModel.richiestaServiziLocalizzazione.observe(viewLifecycleOwner, Observer { resolvable ->
//            resolvable?.let {
//                try {
//                    it.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    setPosizioneStatica()
//                }
//            }
//        })

        // Controlla i permessi e ottieni la posizione
        if (checkLocationPermission()) {
            ricercaTutorViewModel.getPosizioneStudente()
        } else {
            requestLocationPermission()
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
        // Resetta il binding a null per evitare memory leaks
        _binding = null
    }


}



