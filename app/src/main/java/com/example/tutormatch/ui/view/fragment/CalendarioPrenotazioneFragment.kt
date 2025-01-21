package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.FragmentCalendarioPrenotazioneBinding
import com.example.tutormatch.ui.adapter.SelezioneFasceOrarieAdapter
import com.example.tutormatch.ui.view.activity.HomeActivity
import com.example.tutormatch.ui.viewmodel.CalendarioViewModel
import com.example.tutormatch.ui.viewmodel.PrenotazioneViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarioPrenotazioneFragment : Fragment() {

    private lateinit var _binding: FragmentCalendarioPrenotazioneBinding // Binding per il layout del fragment
    private lateinit var calendarioViewModel: CalendarioViewModel // ViewModel per la disponibilità del tutor
    private lateinit var prenotazioneViewModel: PrenotazioneViewModel // ViewModel per la gestione delle prenotazioni
    private lateinit var selezioneFasceOrarieAdapter: SelezioneFasceOrarieAdapter // Adapter per le fasce orarie
    private var selectedDate: String? = null // Data selezionata dall'utente
    private lateinit var annuncioIdSel: String // ID dell'annuncio selezionato
    private lateinit var idStudente: String // ID dello studente
    private lateinit var nome: String // Nome dello studente
    private lateinit var cognome: String // Cognome dello studente
    private val listaFasceSelezionate = mutableListOf<Calendario>() // Lista delle fasce orarie selezionate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioPrenotazioneBinding.inflate(inflater, container, false)

        // Inizializzazione dei ViewModel
        calendarioViewModel = ViewModelProvider(this)[CalendarioViewModel::class.java]
        prenotazioneViewModel = ViewModelProvider(this)[PrenotazioneViewModel::class.java]

        // Recupera i parametri passati al fragment
        idStudente = arguments?.getString("userId").toString()
        nome = arguments?.getString("nome").toString()
        cognome = arguments?.getString("cognome").toString()
        annuncioIdSel = arguments?.getString("annuncioId").toString()

        // Controlla se il tutor è disponibile e configura il RecyclerView
        lifecycleScope.launch {
            val tutorTrovato = calendarioViewModel.getTutorDaAnnuncio(annuncioIdSel)
            if (!tutorTrovato) {
                (activity as? HomeActivity)?.replaceFragment(
                    RicercaTutorFragment(),
                    userId = idStudente,
                    nome = nome,
                    cognome = cognome
                )
            } else {
                setupRecyclerView()
            }
        }

        // Imposta la data minima selezionabile (domani)
        val data = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        val domani = data.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).apply {
            timeZone = TimeZone.getTimeZone("Europe/Rome")
        }
        selectedDate = dateFormat.format(domani)
        _binding.calendarView.minDate = domani.time

        // Listener per il cambio di data nel CalendarView
        _binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            calendarioViewModel.loadDisponibilita()
        }

        // Osserva le fasce orarie disponibili
        calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
            val filteredList = listaDisponibilita.filter {
                dateFormat.format(it.data) == selectedDate && !it.statoPren
            }
            val listaOrdinata = calendarioViewModel.ordinaFasceOrarie(filteredList)
            selezioneFasceOrarieAdapter.setFasceOrarie(listaOrdinata)
        }

        // Mostra messaggi dal ViewModel
        calendarioViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Listener per il pulsante "Prenota"
        _binding.btnPrenota.setOnClickListener {
            if (listaFasceSelezionate.isNotEmpty()) {
                prenotazioneViewModel.creaPrenotazioni(listaFasceSelezionate, idStudente, annuncioIdSel)
            } else {
                Toast.makeText(requireContext(), "Seleziona almeno una fascia oraria", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener per il pulsante "Chiudi"
        _binding.btnChiudi.setOnClickListener {
            (activity as? HomeActivity)?.replaceFragment(
                RicercaTutorFragment(),
                userId = idStudente,
                nome = nome,
                cognome = cognome
            )
        }

        // Osserva il risultato delle prenotazioni
        prenotazioneViewModel.prenotazioneSuccesso.observe(viewLifecycleOwner) { successo ->
            if (successo) {
                Toast.makeText(requireContext(), "Prenotazioni aggiornate!", Toast.LENGTH_SHORT).show()
                calendarioViewModel.loadDisponibilita() // Aggiorna le disponibilità
            } else {
                Toast.makeText(requireContext(), "Errore durante la prenotazione.\nProva a riselezionare il giorno", Toast.LENGTH_SHORT).show()
            }
        }

        return _binding.root
    }

    // Configura il RecyclerView
    private fun setupRecyclerView() {
        selezioneFasceOrarieAdapter = SelezioneFasceOrarieAdapter { fasceSelezionate ->
            listaFasceSelezionate.clear()
            listaFasceSelezionate.addAll(fasceSelezionate)
        }
        _binding.recyclerViewOrariDisponibili.layoutManager = LinearLayoutManager(context)
        _binding.recyclerViewOrariDisponibili.adapter = selezioneFasceOrarieAdapter
    }
}
