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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentCalendarioTutorBinding
import com.example.tutormatch.ui.adapter.CalendarioAdapter
import com.example.tutormatch.ui.viewmodel.CalendarioViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarioFragment : Fragment() {

    private lateinit var _binding: FragmentCalendarioTutorBinding

    private lateinit var calendarioViewModel: CalendarioViewModel
    private lateinit var calendarioAdapter: CalendarioAdapter

    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioTutorBinding.inflate(inflater, container, false)

        calendarioViewModel = ViewModelProvider(this)[CalendarioViewModel::class.java]

        calendarioViewModel.setUpdateOrariInizioCallback {
            updateOrariInizioSpinner(selectedDate)
            updateOrariFineSpinner(selectedDate)
        }

        val userId = arguments?.getString("userId")
        userId?.let {
            val firestore = FirebaseFirestore.getInstance()
            val tutorRef = firestore.collection("utenti").document(it)
            calendarioViewModel.setTutorReference(tutorRef)
        }

        setupRecyclerView()

        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = dateFormat.format(today)
        _binding.calendarView.minDate = today.time

        _binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            updateOrariInizioSpinner(selectedDate)
            calendarioViewModel.loadDisponibilita()
        }

        updateOrariInizioSpinner(selectedDate)

        _binding.spinnerOrariInizio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateOrariFineSpinner(selectedDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Non fare nulla
            }
        }

        _binding.buttonAggiungiDisponibilita.setOnClickListener {
            if (_binding.spinnerOrariInizio.adapter.isEmpty || _binding.spinnerOrariFine.adapter.isEmpty) {
                Toast.makeText(context, "Orari Terminati", Toast.LENGTH_SHORT).show()
            } else {
                val oraInizioSelezionata = _binding.spinnerOrariInizio.selectedItem as String
                val oraFineSelezionata = _binding.spinnerOrariFine.selectedItem as String
                calendarioViewModel.oraInizio.value = oraInizioSelezionata
                calendarioViewModel.oraFine.value = oraFineSelezionata
                calendarioViewModel.data.value = selectedDate
                if (calendarioViewModel.salvaDisponibilita()) {
                    updateOrariInizioSpinner(selectedDate)
                } else {
                    Toast.makeText(context, "Errore: tutti i campi devono essere compilati", Toast.LENGTH_SHORT).show()
                }
            }
        }

        calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
            val filteredList = listaDisponibilita.filter { dateFormat.format(it.data) == selectedDate }
            calendarioAdapter.setCalendari(filteredList)
        }

        calendarioViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return _binding.root
    }

    private fun setupRecyclerView() {
        calendarioAdapter = CalendarioAdapter(
            { calendario -> calendarioViewModel.eliminaDisponibilita(calendario) }
        )
        _binding.recyclerViewDisponibilita.layoutManager = LinearLayoutManager(context)
        _binding.recyclerViewDisponibilita.adapter = calendarioAdapter
    }

    private fun updateOrariInizioSpinner(selectedDate: String?) {
        calendarioViewModel.caricaDisponibilitaPerData(selectedDate) { existingOrari ->
            val orari = calendarioViewModel.generateOrari(selectedDate, existingOrari)
            val adapterInizio = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orari)
            adapterInizio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            _binding.spinnerOrariInizio.adapter = adapterInizio
        }
    }

    private fun updateOrariFineSpinner(selectedDate: String?) {
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
                val orari = calendarioViewModel.generateOrari(selectedDate, incrementedOrari)
                val orariFiltrati = orari.filter { it > orarioInizioSelezionato }.toMutableList()

                if (!existingOrari.contains("23:00")) {
                    orariFiltrati.add("00:00")
                }
                val adapterFineAggiornato = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orariFiltrati)
                adapterFineAggiornato.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                _binding.spinnerOrariFine.adapter = adapterFineAggiornato
            }
            else{
                val emptyAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                _binding.spinnerOrariFine.adapter = emptyAdapter
            }
        }
    }
}
