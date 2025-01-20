package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentCalendarioTutorBinding
import com.example.tutormatch.ui.adapter.CalendarioAdapter
import com.example.tutormatch.ui.viewmodel.CalendarioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarioFragment : Fragment() {

    private lateinit var _binding: FragmentCalendarioTutorBinding // Binding per il layout del fragment
    private lateinit var calendarioViewModel: CalendarioViewModel // ViewModel per la gestione dei dati
    private lateinit var calendarioAdapter: CalendarioAdapter // Adapter per il RecyclerView
    private lateinit var selectedDate: String // Data selezionata dall'utente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il ViewModel e imposta il riferimento al tutor
        calendarioViewModel = ViewModelProvider(this)[CalendarioViewModel::class.java]
        val userIdTutor = requireArguments().getString("userId")!!
        calendarioViewModel.setTutorReference(userIdTutor)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioTutorBinding.inflate(inflater, container, false)

        // Imposta il callback per aggiornare gli spinner degli orari
        calendarioViewModel.setUpdateOrariInizioCallback {
            updateOrariInizioSpinner(selectedDate)
            updateOrariFineSpinner(selectedDate)
        }

        setupRecyclerView() // Configura il RecyclerView

        // Imposta la data minima selezionabile come domani
        val domani = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).apply {
            timeZone = TimeZone.getTimeZone("Europe/Rome")
        }
        selectedDate = dateFormat.format(domani)
        _binding.calendarView.minDate = domani.time

        // Listener per il cambio di data nel calendario
        _binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            updateOrariInizioSpinner(selectedDate)
            calendarioViewModel.loadDisponibilita()
        }

        updateOrariInizioSpinner(selectedDate)

        // Listener per il cambio di selezione nello spinner degli orari di inizio
        _binding.spinnerOrariInizio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateOrariFineSpinner(selectedDate)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Listener per il pulsante "Aggiungi Disponibilità"
        _binding.buttonAggiungiDisponibilita.setOnClickListener {
            if (_binding.spinnerOrariInizio.adapter.isEmpty || _binding.spinnerOrariFine.adapter.isEmpty) {
                Toast.makeText(context, "Orari Terminati", Toast.LENGTH_SHORT).show()
            } else {
                val oraInizioSelezionata = _binding.spinnerOrariInizio.selectedItem as String
                val oraFineSelezionata = _binding.spinnerOrariFine.selectedItem as String
                calendarioViewModel.oraInizio.value = oraInizioSelezionata
                calendarioViewModel.oraFine.value = oraFineSelezionata
                calendarioViewModel.data.value = selectedDate

                // Disabilita il pulsante durante l'operazione
                _binding.buttonAggiungiDisponibilita.isEnabled = false

                lifecycleScope.launch {
                    val success = calendarioViewModel.salvaDisponibilita()
                    if (success) {
                        updateOrariInizioSpinner(selectedDate)
                        _binding.buttonAggiungiDisponibilita.isEnabled = true
                    } else {
                        Toast.makeText(context, "Errore: tutti i campi devono essere compilati", Toast.LENGTH_SHORT).show()
                        _binding.buttonAggiungiDisponibilita.isEnabled = true
                    }
                }
            }
        }

        // Osserva le modifiche nella lista delle disponibilità
        calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
            val filteredList = listaDisponibilita.filter { dateFormat.format(it.data) == selectedDate }
            val listaOrdinata = calendarioViewModel.ordinaFasceOrarie(filteredList)
            calendarioAdapter.setCalendari(listaOrdinata)
        }

        // Mostra eventuali messaggi dal ViewModel
        calendarioViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return _binding.root
    }

    // Configura il RecyclerView con l'adapter
    private fun setupRecyclerView() {
        calendarioAdapter = CalendarioAdapter { calendario ->
            calendarioViewModel.eliminaDisponibilita(calendario)
        }
        _binding.recyclerViewDisponibilita.layoutManager = LinearLayoutManager(context)
        _binding.recyclerViewDisponibilita.adapter = calendarioAdapter
    }

    // Aggiorna lo spinner degli orari di inizio
    private fun updateOrariInizioSpinner(selectedDate: String) {
        calendarioViewModel.caricaDisponibilitaPerData(selectedDate) { existingOrari ->
            val orari = calendarioViewModel.generateOrari(existingOrari)
            val adapterInizio = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orari)
            adapterInizio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            _binding.spinnerOrariInizio.adapter = adapterInizio
        }
    }

    // Aggiorna lo spinner degli orari di fine
    private fun updateOrariFineSpinner(selectedDate: String) {
        calendarioViewModel.caricaDisponibilitaPerData(selectedDate) { existingOrari ->
            val dateFormat = SimpleDateFormat("HH:mm")
            val incrementedOrari = existingOrari.map {
                val calendar = Calendar.getInstance().apply {
                    time = dateFormat.parse(it)
                    add(Calendar.MINUTE, 60)
                }
                dateFormat.format(calendar.time)
            }
            val orarioInizioSelezionato = _binding.spinnerOrariInizio.selectedItem as? String
            if (orarioInizioSelezionato != null) {
                val orari = calendarioViewModel.generateOrari(incrementedOrari)
                val orariFiltrati = orari.filter { it > orarioInizioSelezionato }.toMutableList()
                if (!existingOrari.contains("23:00")) {
                    orariFiltrati.add("00:00")
                }
                val adapterFineAggiornato = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orariFiltrati)
                adapterFineAggiornato.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                _binding.spinnerOrariFine.adapter = adapterFineAggiornato
            } else {
                val emptyAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                _binding.spinnerOrariFine.adapter = emptyAdapter
            }
        }
    }
}
