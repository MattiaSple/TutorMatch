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
import com.example.tutormatch.databinding.FragmentCalendarioTutorBinding
import com.example.tutormatch.ui.viewmodel.CalendarioViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarioFragment : Fragment() {

    private lateinit var _binding: FragmentCalendarioTutorBinding
    private val binding get() = _binding
    private lateinit var calendarioViewModel: CalendarioViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioTutorBinding.inflate(inflater, container, false)

        calendarioViewModel = ViewModelProvider(this).get(CalendarioViewModel::class.java)

        val userId = arguments?.getString("userId")
        userId?.let {
            val firestore = FirebaseFirestore.getInstance()
            val tutorRef = firestore.collection("utenti").document(it)
            calendarioViewModel.setTutorReference(tutorRef)

            // Configura il calendario
            val today = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayString = dateFormat.format(today)

            binding.calendarView.minDate = today.time
            var selectedDate = todayString

            binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                updateOrariInizioSpinner(selectedDate)
            }

            // Configura il menu a tendina degli orari
            updateOrariInizioSpinner(todayString)

            // Aggiungi listener allo Spinner di inizio
            binding.spinnerOrariInizio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val orarioInizioSelezionato = binding.spinnerOrariInizio.selectedItem as String
                    val orari = generateOrari(selectedDate)
                    val orariFiltrati = orari.filter { it > orarioInizioSelezionato }
                    val adapterFineAggiornato = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orariFiltrati)
                    adapterFineAggiornato.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerOrariFine.adapter = adapterFineAggiornato
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Non fare nulla
                }
            }

            // Aggiungi disponibilità
            binding.buttonAggiungiDisponibilita.setOnClickListener {
                val oraInizioSelezionata = binding.spinnerOrariInizio.selectedItem as String
                val oraFineSelezionata = binding.spinnerOrariFine.selectedItem as String
                selectedDate?.let { data ->
                    calendarioViewModel.aggiungiDisponibilita(data, oraInizioSelezionata, oraFineSelezionata)
                }
            }

            // Osserva le disponibilità
            calendarioViewModel.lista_disponibilita.observe(viewLifecycleOwner) { listaDisponibilita ->
                val disponibilitaString = listaDisponibilita.map {
                    "${dateFormat.format(it.data)} - ${it.oraInizio} alle ${it.oraFine} - ${if (it.stato_pren) "Prenotato" else "Disponibile"}"
                }
                val listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, disponibilitaString)
                binding.listViewDisponibilita.adapter = listAdapter
            }

            // Osserva i messaggi
            calendarioViewModel.message.observe(viewLifecycleOwner) { message ->
                message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    private fun generateOrari(selectedDate: String? = null): List<String> {
        val orari = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Controlla se la data selezionata è oggi
        val oggi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (selectedDate == oggi) {
            // Imposta l'orario iniziale al prossimo intervallo di 30 minuti futuro
            val currentTime = Calendar.getInstance()
            currentTime.add(Calendar.MINUTE, 30 - (currentTime.get(Calendar.MINUTE) % 30))
            calendar.time = currentTime.time
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        }

        var counter = 0
        while (counter < 48) {
            orari.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.MINUTE, 30)
            counter++
        }

        return orari
    }

    private fun updateOrariInizioSpinner(selectedDate: String?) {
        val orari = generateOrari(selectedDate)
        val adapterInizio = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orari)
        adapterInizio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrariInizio.adapter = adapterInizio
    }
}
