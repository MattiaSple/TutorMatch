package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentHomeTutorBinding
import com.example.tutormatch.ui.adapter.AnnuncioAdapter
import com.example.tutormatch.ui.viewmodel.AnnuncioViewModel
import kotlinx.coroutines.launch

class HomeFragmentTutor : Fragment() {

    private lateinit var _binding: FragmentHomeTutorBinding
    private val binding get() = _binding
    private lateinit var annuncioViewModel: AnnuncioViewModel
    private lateinit var annuncioAdapter: AnnuncioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializzazione del ViewModel in onCreate (non è legato alla vista)
        annuncioViewModel = ViewModelProvider(this)[AnnuncioViewModel::class.java]

        // Se c'è un userId, passarlo al ViewModel
        val userId = arguments?.getString("userId")
        userId?.let {
            annuncioViewModel.setTutorReference(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflaziona il layout con il binding
        _binding = FragmentHomeTutorBinding.inflate(inflater, container, false)

        // Ritorna la vista associata al Fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Imposta il lifecycleOwner per il data binding
        binding.lifecycleOwner = viewLifecycleOwner

        // Configura la RecyclerView dopo che la vista è stata creata
        setupRecyclerView()

        binding.buttonSalva.setOnClickListener {
            binding.buttonSalva.isEnabled = false

            val materia = binding.spinnerMateria.selectedItem.toString()
            val prezzo = binding.editTextNumber.text.toString()
            val descrizione = binding.editTextDescrizione.text.toString()
            val online = binding.checkBoxOnline.isChecked
            val presenza = binding.checkBoxPresenza.isChecked

            lifecycleScope.launch {
                annuncioViewModel.salvaAnnuncio(materia, prezzo, descrizione, online, presenza)
                binding.buttonSalva.isEnabled = true
            }
        }



        // Osserva la lista di annunci dal ViewModel
        annuncioViewModel.listaAnnunciTutor.observe(viewLifecycleOwner) { listaAnnunci ->
            annuncioAdapter.setAnnunci(listaAnnunci)
            annuncioAdapter.notifyDataSetChanged()
        }

        // Osserva i messaggi dal ViewModel per mostrare i Toast
        annuncioViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        // Inizializza l'adapter per la RecyclerView
        annuncioAdapter = AnnuncioAdapter { annuncio ->
            annuncioViewModel.eliminaAnnuncio(annuncio)
        }
        binding.recyclerViewAnnunci.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAnnunci.adapter = annuncioAdapter
    }
}


