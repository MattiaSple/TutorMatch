package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tutormatch.databinding.FragmentPrenotazioniStudenteBinding
import com.example.tutormatch.ui.viewmodel.PrenotazioniViewModel

class PrenotazioniFragment : Fragment() {

    private lateinit var _binding: FragmentPrenotazioniStudenteBinding
    private val binding get() = _binding!!

    private val prenotazioniViewModel: PrenotazioniViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrenotazioniStudenteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val adapter = PrenotazioniAdapter(prenotazioniViewModel.prenotazioni.value ?: emptyList()) { prenotazione ->
//            prenotazioniViewModel.eliminaPrenotazione(prenotazione)
//        }
//        binding.recyclerViewPrenotazioni.layoutManager = LinearLayoutManager(context)
//        binding.recyclerViewPrenotazioni.adapter = adapter
//
//        prenotazioniViewModel.prenotazioni.observe(viewLifecycleOwner, Observer { prenotazioni ->
//            adapter.notifyDataSetChanged()
//        })
//    }

}
