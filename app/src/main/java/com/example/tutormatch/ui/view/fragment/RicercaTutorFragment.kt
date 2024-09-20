package com.example.tutormatch.ui.view.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.tutormatch.R
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.databinding.FragmentRicercaTutorBinding
import com.example.tutormatch.ui.view.activity.HomeActivity
import com.example.tutormatch.ui.viewmodel.AnnunciViewModel
import com.example.tutormatch.ui.viewmodel.ChatViewModel
import com.example.tutormatch.ui.viewmodel.RicercaTutorViewModel
import com.example.tutormatch.ui.viewmodel.SharedViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.widget.ArrayAdapter
import android.widget.SeekBar

import androidx.lifecycle.ViewModelProvider

class RicercaTutorFragment : Fragment() {

    private var _binding: FragmentRicercaTutorBinding? = null
    private val binding get() = _binding!!

    // Inizializziamo i ViewModel usando il delegate di Kotlin
    private lateinit var ricercaTutorViewModel: RicercaTutorViewModel
    private lateinit var annunciViewModel: AnnunciViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var mapView: MapView
    private lateinit var locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>

    // Mappa per tenere traccia dei marker sulla mappa
    private val markerMap = mutableMapOf<String, Marker>()

    // Gestione delle richieste dei permessi di localizzazione
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
        ricercaTutorViewModel = ViewModelProvider(this).get(RicercaTutorViewModel::class.java)
        annunciViewModel = ViewModelProvider(this).get(AnnunciViewModel::class.java)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
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
                    title = "Sono Qui"
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
        annunciViewModel.aggiornaListaAnnunciInTempoReale()

        annunciViewModel.listaAnnunciFiltrati.observe(viewLifecycleOwner, Observer { listaFiltrata ->
            aggiornaMappa(listaFiltrata)  // Mostra solo gli annunci filtrati
        })

        binding.applyButton.setOnClickListener {
            if(binding.inPersonCheckBox.isChecked || binding.onlineCheckBox.isChecked)
            {

                val materiaSelezionata = binding.subjectSpinner.selectedItem.toString()
                val budgetSelezionato = binding.budgetSeekBar.progress  // Valore del budget dalla SeekBar
                val isOnlineChecked = binding.onlineCheckBox.isChecked  // Stato della checkbox "Online"
                val isInPersonChecked = binding.inPersonCheckBox.isChecked  // Stato della checkbox "In presenza"
                annunciViewModel.flagFiltro = true
                // Applica i filtri con i parametri ottenuti dall'utente
                annunciViewModel.filtraAnnunciMappa(materiaSelezionata, budgetSelezionato, isOnlineChecked, isInPersonChecked)
            }else{
                Toast.makeText(context,"Inserire la modalità", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resetButton.setOnClickListener{
            if(binding.budgetSeekBar.progress != 0 || binding.onlineCheckBox.isChecked || binding.inPersonCheckBox.isChecked)
            {
                binding.budgetSeekBar.progress = 0
                binding.onlineCheckBox.isChecked = false
                binding.inPersonCheckBox.isChecked = false
                annunciViewModel.flagFiltro = false
                annunciViewModel.svuotaListaAnnunciFiltrati()
            }
        }

        annunciViewModel.listaAnnunci.observe(viewLifecycleOwner, Observer {
            if(annunciViewModel.flagFiltro)
            {
                annunciViewModel.filtraAnnunciMappa(binding.subjectSpinner.selectedItem.toString(),binding.budgetSeekBar.progress, binding.onlineCheckBox.isChecked, binding.inPersonCheckBox.isChecked )
            }
        })

        // Definisci una lista di materie
        val materie = listOf("Matematica", "Fisica", "Informatica", "Chimica", "Biologia")

        // Crea un ArrayAdapter per lo Spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            materie
        )

        // Imposta lo stile per la visualizzazione a tendina
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Collega l'adapter allo Spinner
        binding.subjectSpinner.adapter = adapter


        // Trova il riferimento alla SeekBar e al TextView del valore
        val budgetSeekBar = binding.budgetSeekBar
        val budgetValueText = binding.budgetValueText

        // Imposta un listener per aggiornare il valore del TextView quando l'utente cambia il valore della SeekBar
        budgetSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Aggiorna il testo del TextView con il valore attuale della SeekBar
                budgetValueText.text = "$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Non è necessario fare nulla qui
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Non è necessario fare nulla qui
            }
        })

    }

    private fun showMarkerDetails(marker: Marker, annuncio: Annuncio) {
        annuncio.tutor?.get()?.addOnSuccessListener { documentSnapshot ->
            val nomeTutor = documentSnapshot.getString("nome")!!
            val cognomeTutor = documentSnapshot.getString("cognome")!!
            val tutorEmail = documentSnapshot.getString("email")!!

            // Imposta i dati nei TextView
            binding.tvMateria.text = "$nomeTutor $cognomeTutor"
            binding.tvNomeCognome.text = annuncio.materia
            binding.tvDescription.text = annuncio.descrizione

            // Mostra il layout
            binding.markerDetailCard.visibility = View.VISIBLE

            // Gestione del bottone per chattare
            binding.btnChat.setOnClickListener {

                // Ora puoi creare la chat passando i dati dell'utente e del tutor
                chatViewModel.creaChatConTutor(
                    tutorEmail = tutorEmail,
                    tutorName = nomeTutor,
                    tutorSurname = cognomeTutor,
                    userName = arguments?.getString("nome") ?: "",
                    userSurname = arguments?.getString("cognome") ?: "",
                    materia = annuncio.materia,
                    onSuccess = { chatId ->
                        // Passa alla schermata della chat
                        sharedViewModel.setChatId(chatId)
                        (activity as? HomeActivity)?.replaceFragment(
                            ChatFragment(),
                            userId = arguments?.getString("userId") ?: "",
                            nome = arguments?.getString("nome") ?: "",
                            cognome = arguments?.getString("cognome") ?: "",
                            ruolo = arguments?.getBoolean("ruolo") ?: false
                        )
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(context, "Errore: $errorMessage", Toast.LENGTH_SHORT).show()
                    },
                    onConfirm = { message, onConfirmAction ->
                        // Mostra un dialogo di conferma all'utente
                        showConfirmationDialog(message, onConfirmAction)
                    }
                )
            }

            // Gestione del bottone per chiudere
            binding.btnClose.setOnClickListener {
                binding.markerDetailCard.visibility = View.GONE
            }
        }?.addOnFailureListener {
            Toast.makeText(context, "Impossibile ottenere i dettagli del tutor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog(message: String, onConfirmAction: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Sì") { _, _ ->
                // Se l'utente conferma, esegui l'azione di conferma
                onConfirmAction()
            }
            .setNegativeButton("No", null)
            .show()
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

            // Mappa per tracciare quante volte una posizione è stata usata
            val posizioneMap = mutableMapOf<GeoPoint, Int>()

            // Aggiungi nuovi marker e aggiorna quelli esistenti
            for (annuncio in listaAnnunci) {
                val osmdroidGeoPoint = GeoPoint(annuncio.posizione.latitude, annuncio.posizione.longitude)

                // Controlla quante volte la stessa posizione è stata usata
                val count = posizioneMap.getOrDefault(osmdroidGeoPoint, 0)

                // Se ci sono più marker sulla stessa posizione, applica un piccolo offset
                val offset = count * 0.0001
                val adjustedGeoPoint = GeoPoint(
                    osmdroidGeoPoint.latitude + offset,
                    osmdroidGeoPoint.longitude + offset
                )

                // Aggiungi un nuovo marker se non esiste già
                if (!markerMap.containsKey(annuncio.id)) {
                    val marker = Marker(mapView).apply {
                        position = adjustedGeoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = annuncio.materia
                        snippet = annuncio.descrizione
                        icon = resources.getDrawable(R.drawable.marker_annuncio, null)
                        setOnMarkerClickListener { clickedMarker, _ ->
                            showMarkerDetails(clickedMarker, annuncio)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                    markerMap[annuncio.id] = marker  // Aggiungi il marker alla mappa dei marker
                } else {
                    // Aggiorna la posizione del marker esistente se necessario
                    val marker = markerMap[annuncio.id]
                    marker?.position = adjustedGeoPoint
                }

                // Aggiorna il conteggio delle posizioni duplicate
                posizioneMap[osmdroidGeoPoint] = count + 1
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
