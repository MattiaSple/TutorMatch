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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioTutorBinding.inflate(inflater, container, false)

        calendarioViewModel = ViewModelProvider(this).get(CalendarioViewModel::class.java)

        val userId = arguments?.getString("userId")
        userId?.let {
            val firestore = FirebaseFirestore.getInstance()
            val tutorRef = firestore.collection("utenti").document(it)
            calendarioViewModel.setTutorReference(tutorRef)
        }

        setupRecyclerView()

        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayString = dateFormat.format(today)

        binding.calendarView.minDate = today.time
        var selectedDate = todayString

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            updateOrariInizioSpinner(selectedDate)
        }

        updateOrariInizioSpinner(todayString)

        binding.spinnerOrariInizio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateOrariFineSpinner(selectedDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Non fare nulla
            }
        }

        binding.buttonAggiungiDisponibilita.setOnClickListener {
            val oraInizioSelezionata = binding.spinnerOrariInizio.selectedItem as String
            val oraFineSelezionata = binding.spinnerOrariFine.selectedItem as String
            calendarioViewModel.oraInizio.value = oraInizioSelezionata
            calendarioViewModel.oraFine.value = oraFineSelezionata
            calendarioViewModel.data.value = selectedDate
            if (calendarioViewModel.salvaDisponibilita()) {
                updateOrariInizioSpinner(selectedDate)
                updateOrariFineSpinner(selectedDate)
                Toast.makeText(context, "Disponibilità salvata con successo", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Errore: tutti i campi devono essere compilati", Toast.LENGTH_SHORT).show()
            }
        }

        calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
            calendarioAdapter.setCalendari(listaDisponibilita)
        }

        calendarioViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        calendarioAdapter = CalendarioAdapter { calendario ->
            calendarioViewModel.eliminaDisponibilita(calendario)
        }
        binding.recyclerViewDisponibilita.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewDisponibilita.adapter = calendarioAdapter
    }

    private fun generateOrari(selectedDate: String?, existingOrari: List<String>): List<String> {
        val orari = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val oggi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (selectedDate == oggi) {
            val currentTime = Calendar.getInstance()
            currentTime.add(Calendar.MINUTE, 60 - (currentTime.get(Calendar.MINUTE) % 60))
            calendar.time = currentTime.time
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        }

        var counter = 0
        while (counter < 24) {
            val orario = dateFormat.format(calendar.time)
            if (!existingOrari.contains(orario)) {
                orari.add(orario)
            }
            calendar.add(Calendar.MINUTE, 60)
            counter++
        }

        return orari
    }

    private fun updateOrariInizioSpinner(selectedDate: String?) {
        calendarioViewModel.caricaDisponibilitaPerData(selectedDate) { existingOrari ->
            val orari = generateOrari(selectedDate, existingOrari)
            val adapterInizio = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orari)
            adapterInizio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerOrariInizio.adapter = adapterInizio
        }
    }

    private fun updateOrariFineSpinner(selectedDate: String?) {
        calendarioViewModel.caricaDisponibilitaPerData(selectedDate) { existingOrari ->
            val orarioInizioSelezionato = binding.spinnerOrariInizio.selectedItem as String
            val orari = generateOrari(selectedDate, existingOrari)
            val orariFiltrati = orari.filter { it > orarioInizioSelezionato }
            val adapterFineAggiornato = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orariFiltrati)
            adapterFineAggiornato.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerOrariFine.adapter = adapterFineAggiornato
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
