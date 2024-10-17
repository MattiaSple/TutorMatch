package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentHomeTutorBinding
import com.example.tutormatch.ui.adapter.AnnuncioAdapter
import com.example.tutormatch.ui.viewmodel.AnnunciViewModel
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragmentTutor : Fragment() {

    private lateinit var _binding: FragmentHomeTutorBinding
    private val binding get() = _binding
    private lateinit var annunciViewModel: AnnunciViewModel
    private lateinit var annuncioAdapter: AnnuncioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializzazione del ViewModel in onCreate (non è legato alla vista)
        annunciViewModel = ViewModelProvider(this).get(AnnunciViewModel::class.java)

        // Se c'è un userId, passarlo al ViewModel
        val userId = arguments?.getString("userId")
        userId?.let {
            annunciViewModel.setTutorReference(it)
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

        // Imposta il listener per il bottone "Salva"
        binding.buttonSalva.setOnClickListener {
            val materia = binding.spinnerMateria.selectedItem.toString()
            val prezzo = binding.editTextNumber.text.toString()
            val descrizione = binding.editTextDescrizione.text.toString()
            val online = binding.checkBoxOnline.isChecked
            val presenza = binding.checkBoxPresenza.isChecked

            // Chiama il ViewModel per salvare l'annuncio
            val userId = arguments?.getString("userId")
            userId?.let {
                annunciViewModel.salvaAnnuncio(it, materia, prezzo, descrizione, online, presenza)
            }
        }

        // Osserva la lista di annunci dal ViewModel
        annunciViewModel.listaAnnunciTutor.observe(viewLifecycleOwner) { listaAnnunci ->
            annuncioAdapter.setAnnunci(listaAnnunci)
            annuncioAdapter.notifyDataSetChanged()
        }

        // Osserva i messaggi dal ViewModel per mostrare i Toast
        annunciViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        // Inizializza l'adapter per la RecyclerView
        annuncioAdapter = AnnuncioAdapter { annuncio ->
            annunciViewModel.eliminaAnnuncio(annuncio)
        }
        binding.recyclerViewAnnunci.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAnnunci.adapter = annuncioAdapter
    }
}


