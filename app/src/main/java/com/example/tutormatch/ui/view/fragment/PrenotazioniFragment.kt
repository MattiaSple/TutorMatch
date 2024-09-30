package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentPrenotazioniBinding
import com.example.tutormatch.ui.adapter.PrenotazioneAdapter
import com.example.tutormatch.ui.viewmodel.PrenotazioneViewModel

class PrenotazioniFragment : Fragment() {

    private var _binding: FragmentPrenotazioniBinding? = null
    private val binding get() = _binding!!
    private val prenotazioneViewModel: PrenotazioneViewModel by viewModels()
    private lateinit var adapterPrenotazione: PrenotazioneAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrenotazioniBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("userId")
        val ruolo = arguments?.getBoolean("ruolo")

        adapterPrenotazione = PrenotazioneAdapter(emptyList(), ruolo!!) { prenotazione ->
            // Gestisci il click per eliminare la prenotazione, se necessario
        }
        binding.recyclerViewPrenotazioni.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPrenotazioni.adapter = adapterPrenotazione

        // Carica le prenotazioni dell'utente
        userId?.let {
            prenotazioneViewModel.caricaPrenotazioni(ruolo, it)
        }

        // Osserva i cambiamenti delle prenotazioni nel ViewModel
        prenotazioneViewModel.listaPrenotazioni.observe(viewLifecycleOwner, Observer { prenotazioni ->
            adapterPrenotazione.updatePrenotazioni(prenotazioni)
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
