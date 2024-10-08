package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.FragmentCalendarioPrenotazioneBinding
import com.example.tutormatch.ui.adapter.SelezioneFasceOrarieAdapter
import com.example.tutormatch.ui.view.activity.HomeActivity
import com.example.tutormatch.ui.viewmodel.CalendarioViewModel
import com.example.tutormatch.ui.viewmodel.PrenotazioneViewModel

import java.text.SimpleDateFormat
import java.util.*

class CalendarioPrenotazioneFragment : Fragment() {

    private lateinit var _binding: FragmentCalendarioPrenotazioneBinding
    private lateinit var calendarioViewModel: CalendarioViewModel
    private lateinit var prenotazioneViewModel: PrenotazioneViewModel
    private lateinit var selezioneFasceOrarieAdapter: SelezioneFasceOrarieAdapter
    private var selectedDate: String? = null
    private lateinit var annuncioIdSel: String
    private lateinit var idStudente: String

    private val listaFasceSelezionate = mutableListOf<Calendario>() // Fasce selezionate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioPrenotazioneBinding.inflate(inflater, container, false)

        // Inizializza i ViewModel
        calendarioViewModel = ViewModelProvider(this)[CalendarioViewModel::class.java]
        prenotazioneViewModel = ViewModelProvider(this)[PrenotazioneViewModel::class.java]

        // Recupera l'annuncioId
        idStudente = arguments?.getString("userId").toString()
        annuncioIdSel = arguments?.getString("annuncioId").toString()


        // Ottieni l'ID del tutor usando il callback
        calendarioViewModel.getTutorDaAnnuncio(annuncioIdSel) { tutorRef ->
            if (tutorRef != null) {
                calendarioViewModel.setTutorReference(tutorRef)
                setupRecyclerView() // Configura il RecyclerView
            } else {
                Toast.makeText(context, "Errore: Tutor non trovato", Toast.LENGTH_SHORT).show()
            }
        }

        // Imposta la data di oggi come predefinita
        val data = Calendar.getInstance()
        data.add(Calendar.DAY_OF_MONTH,1)
        val domani = data.time
        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).apply {
            timeZone = TimeZone.getTimeZone("Europe/Rome")  // Usa il fuso orario italiano
        }
        selectedDate = dateFormat.format(today)
        _binding.calendarView.minDate = today.time

        // Ascolta i cambiamenti di data nel CalendarView
        _binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            calendarioViewModel.loadDisponibilita()

        }

        // Osserva le fasce orarie disponibili per la data selezionata
        calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
            val filteredList = listaDisponibilita.filter { dateFormat.format(it.data) == selectedDate && !it.statoPren}
            val listaOrdinata = calendarioViewModel.ordinaFasceOrarie(filteredList)
            selezioneFasceOrarieAdapter.setFasceOrarie(listaOrdinata)
        }

        calendarioViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Imposta l'azione sul pulsante "Prenota"
        _binding.btnPrenota.setOnClickListener {
            if (listaFasceSelezionate.isNotEmpty()) {
                prenotazioneViewModel.setPrenotazioni(listaFasceSelezionate, idStudente, annuncioIdSel) {
                    Toast.makeText(requireContext(), "Prenotazioni aggiornate!", Toast.LENGTH_SHORT).show()
                    calendarioViewModel.loadDisponibilita()
                }
            } else {
                Toast.makeText(requireContext(), "Seleziona almeno una fascia oraria", Toast.LENGTH_SHORT).show()
            }
        }

        return _binding.root
    }

    private fun setupRecyclerView() {
        selezioneFasceOrarieAdapter = SelezioneFasceOrarieAdapter { fasceSelezionate ->
            listaFasceSelezionate.clear()
            listaFasceSelezionate.addAll(fasceSelezionate)
        }

        _binding.recyclerViewOrariDisponibili.layoutManager = LinearLayoutManager(context)
        _binding.recyclerViewOrariDisponibili.adapter = selezioneFasceOrarieAdapter
    }
}