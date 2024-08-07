package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentCalendarioTutorBinding
import com.example.tutormatch.ui.adapter.CalendarioAdapter
import com.example.tutormatch.ui.viewmodel.CalendarioViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarioFragment : Fragment() {

    private var _binding: FragmentCalendarioTutorBinding? = null
    private val binding get() = _binding!!
    private lateinit var calendarioViewModel: CalendarioViewModel
    private lateinit var calendarioAdapter: CalendarioAdapter

    // Metodo chiamato per creare e restituire la vista gerarchica associata al frammento
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioTutorBinding.inflate(inflater, container, false)

        // Inizializzazione del ViewModel
        calendarioViewModel = ViewModelProvider(this).get(CalendarioViewModel::class.java)

        // Imposta un callback per aggiornare gli orari di inizio
        calendarioViewModel.setUpdateOrariInizioCallback {
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(binding.calendarView.date)
            updateOrariInizioSpinner(selectedDate)
        }

        // Recupera l'ID utente dagli argomenti passati al frammento
        val userId = arguments?.getString("userId")
        userId?.let {
            val firestore = FirebaseFirestore.getInstance()
            val tutorRef = firestore.collection("utenti").document(it)
            calendarioViewModel.setTutorReference(tutorRef)
        }

        setupRecyclerView()

        // Imposta la data di oggi
        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayString = dateFormat.format(today)

        // Imposta la data minima del calendarView
        binding.calendarView.minDate = today.time
        var selectedDate = todayString

        // Listener per il cambio di data nel calendarView
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            updateOrariInizioSpinner(selectedDate)
            calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
                val filteredList = listaDisponibilita.filter { dateFormat.format(it.data) == selectedDate }
                calendarioAdapter.setCalendari(filteredList)
            }
        }

        updateOrariInizioSpinner(todayString)

        // Listener per la selezione degli orari di inizio
        binding.spinnerOrariInizio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateOrariFineSpinner(selectedDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Non fare nulla
            }
        }

        // Listener per il bottone di aggiunta disponibilità
        binding.buttonAggiungiDisponibilita.setOnClickListener {
            val oraInizioSelezionata = binding.spinnerOrariInizio.selectedItem as String
            val oraFineSelezionata = binding.spinnerOrariFine.selectedItem as String
            calendarioViewModel.oraInizio.value = oraInizioSelezionata
            calendarioViewModel.oraFine.value = oraFineSelezionata
            calendarioViewModel.data.value = selectedDate
            if (calendarioViewModel.salvaDisponibilita()) {
                updateOrariInizioSpinner(selectedDate)
            } else {
                Toast.makeText(context, "Errore: tutti i campi devono essere compilati", Toast.LENGTH_SHORT).show()
            }
        }

        // Osservatore per la lista delle disponibilità
        calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
            val filteredList = listaDisponibilita.filter { dateFormat.format(it.data) == selectedDate }
            calendarioAdapter.setCalendari(filteredList)
        }

        // Osservatore per i messaggi
        calendarioViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    // Configurazione del RecyclerView
    private fun setupRecyclerView() {
        calendarioAdapter = CalendarioAdapter(
            { calendario -> calendarioViewModel.eliminaDisponibilita(calendario) },
            { selectedDate -> updateOrariInizioSpinner(selectedDate) }
        )
        binding.recyclerViewDisponibilita.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewDisponibilita.adapter = calendarioAdapter
    }

    // Generazione degli orari disponibili per la data selezionata
    private fun generateOrari(selectedDate: String?, existingOrari: List<String>): List<String> {
        val orari = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val oggi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var oreRimanenti = 24 // Default, in caso non sia oggi

        if (selectedDate == oggi) {
            val currentTime = Calendar.getInstance()
            currentTime.add(Calendar.MINUTE, 60 - (currentTime.get(Calendar.MINUTE) % 60))
            calendar.time = currentTime.time

            // Calcola le ore rimanenti fino a mezzanotte
            oreRimanenti = 24 - currentTime.get(Calendar.HOUR_OF_DAY)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        }

        var counter = 0
        while (counter < oreRimanenti) {
            val orario = dateFormat.format(calendar.time)
            if (!existingOrari.contains(orario)) {
                orari.add(orario)
            }
            calendar.add(Calendar.MINUTE, 60)
            counter++
        }

        return orari
    }

    // Aggiornamento degli orari di inizio nello spinner
    private fun updateOrariInizioSpinner(selectedDate: String?) {
        calendarioViewModel.caricaDisponibilitaPerData(selectedDate) { existingOrari ->
            val orari = generateOrari(selectedDate, existingOrari)
            val adapterInizio = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orari)
            adapterInizio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerOrariInizio.adapter = adapterInizio
        }
    }

    // Aggiornamento degli orari di fine nello spinner
    private fun updateOrariFineSpinner(selectedDate: String?) {
        calendarioViewModel.caricaDisponibilitaPerData(selectedDate) { existingOrari ->
            val orarioInizioSelezionato = binding.spinnerOrariInizio.selectedItem as String
            val orari = generateOrari(selectedDate, existingOrari)
            val orariFiltrati = orari.filter { it > orarioInizioSelezionato }.toMutableList()

            // Controlla se orariFiltrati è vuoto e aggiungi "00:00" se necessario
            if (orariFiltrati.isEmpty()) {
                orariFiltrati.add("00:00")
            }

            val adapterFineAggiornato = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orariFiltrati)
            adapterFineAggiornato.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerOrariFine.adapter = adapterFineAggiornato
        }
    }

    // Metodo chiamato quando la vista viene distrutta
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

