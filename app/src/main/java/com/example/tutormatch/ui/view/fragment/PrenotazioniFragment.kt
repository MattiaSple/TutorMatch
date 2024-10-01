package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentPrenotazioniBinding
import com.example.tutormatch.ui.adapter.AnnuncioAdapter
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


        //setUpRecycle view
        adapterPrenotazione = PrenotazioneAdapter(emptyList(), ruolo!!) { prenotazione ->
            prenotazioneViewModel.eliminaPrenotazione(prenotazione)
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

        // Osserva la LiveData delle notifiche
        prenotazioneViewModel.notificaPrenotazione.observe(viewLifecycleOwner, Observer { messaggio ->
            messaggio?.let {
                // Mostra il pop-up
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })


    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
