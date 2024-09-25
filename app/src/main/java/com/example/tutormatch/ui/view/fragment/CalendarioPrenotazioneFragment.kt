package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.FragmentCalendarioPrenotazioneBinding
import com.example.tutormatch.ui.adapter.SelezioneFasceOrarieAdapter
import com.example.tutormatch.ui.viewmodel.CalendarioViewModel
import com.example.tutormatch.ui.viewmodel.PrenotazioneViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.*

class CalendarioPrenotazioneFragment : Fragment() {

    private lateinit var _binding: FragmentCalendarioPrenotazioneBinding
    private lateinit var calendarioViewModel: CalendarioViewModel
    private lateinit var prenotazioneViewModel: PrenotazioneViewModel
    private lateinit var selezioneFasceOrarieAdapter: SelezioneFasceOrarieAdapter
    private var selectedDate: String? = null
    private lateinit var annuncioIdSel: String
    private lateinit var tutorRef: String

    // Lista per memorizzare le fasce orarie selezionate
    private val fasceSelezionate = mutableListOf<Calendario>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioPrenotazioneBinding.inflate(inflater, container, false)

        // Inizializza i ViewModel
        calendarioViewModel = ViewModelProvider(this)[CalendarioViewModel::class.java]
        prenotazioneViewModel = ViewModelProvider(this)[PrenotazioneViewModel::class.java]

        // Recupera l'annuncio selezionato dagli argomenti
        annuncioIdSel = requireArguments().getString("annuncioId").toString()

        if (annuncioIdSel == null) {
            Toast.makeText(context, "Errore: annuncio non disponibile", Toast.LENGTH_SHORT).show()
            return _binding.root
        }

        // Carica i dati del tutor associato all'annuncio
        val firestore = FirebaseFirestore.getInstance()
        val annuncioDocumentReference = firestore.collection("annunci").document(annuncioIdSel)

        annuncioDocumentReference.get().addOnSuccessListener { documentSnapshot ->
            val annuncio = documentSnapshot.toObject<Annuncio>()
            annuncio?.let {
                tutorRef = it.tutor!!.id
                val tutorDocumentReference = it.tutor
                // Imposta il tutorRef per caricare le fasce orarie del tutor
                calendarioViewModel.setTutorReference(tutorDocumentReference)
            }
        }

        // Configura il RecyclerView
        setupRecyclerView()

        // Imposta la data di oggi come predefinita
        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = dateFormat.format(today)
        _binding.calendarView.minDate = today.time

        // Ascolta i cambiamenti di data nel CalendarView
        _binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            calendarioViewModel.loadDisponibilitaForDate(selectedDate)
        }

        // Osserva le fasce orarie disponibili per la data selezionata
        calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
            val filteredList = listaDisponibilita.filter {
                dateFormat.format(it.data) == selectedDate
            }
            selezioneFasceOrarieAdapter.setFasceOrarie(filteredList)
        }

        // Gestione del pulsante "Prenota"
        _binding.btnPrenota.setOnClickListener {
            if (fasceSelezionate.isNotEmpty()) {
                // Effettua la prenotazione per ogni fascia oraria selezionata
                for (fascia in fasceSelezionate) {
                }
                Toast.makeText(context, "Prenotazioni effettuate con successo", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Seleziona almeno una fascia oraria", Toast.LENGTH_SHORT).show()
            }
        }
        return _binding.root
    }


    // Configura il RecyclerView per mostrare le fasce orarie disponibili
    private fun setupRecyclerView() {
        selezioneFasceOrarieAdapter = SelezioneFasceOrarieAdapter { fasciaOraria ->
            if (fasceSelezionate.contains(fasciaOraria)) {
                fasceSelezionate.remove(fasciaOraria)
            } else {
                fasceSelezionate.add(fasciaOraria)
            }
        }
        _binding.recyclerViewOrariDisponibili.layoutManager = LinearLayoutManager(context)
        _binding.recyclerViewOrariDisponibili.adapter = selezioneFasceOrarieAdapter
    }
}
