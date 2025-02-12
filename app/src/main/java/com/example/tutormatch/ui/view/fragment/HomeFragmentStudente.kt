package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentHomeStudenteBinding
import com.example.tutormatch.ui.adapter.ValutaTutorAdapter
import com.example.tutormatch.ui.viewmodel.HomeViewModel

class HomeFragmentStudente : Fragment() {

    // ViewModel e binding per il fragment
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var _binding: FragmentHomeStudenteBinding
    private val binding get() = _binding
    private lateinit var adapter: ValutaTutorAdapter
    val userId = arguments?.getString("userId")

    // Metodo onCreate: Inizializza elementi non legati alla vista (come il ViewModel)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il ViewModel legato al ciclo di vita del Fragment
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    // Metodo onCreateView: Inflazione del layout con il binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflaziona il layout con il ViewBinding
        _binding = FragmentHomeStudenteBinding.inflate(inflater, container, false)

        return binding.root  // Restituisce la root della vista
    }

    // Metodo onViewCreated: Configura gli elementi della UI e imposta osservatori
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Associa il ViewModel e il ciclo di vita al binding
        binding.viewModel = homeViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val userId = arguments?.getString("userId")
        // Configura la RecyclerView dopo che la vista è stata creata
        val recyclerView = binding.recyclerViewTutors
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inizializza l'adattatore per la lista dei tutor e gestisci il rating
        adapter = ValutaTutorAdapter(emptyList()) { tutor, rating ->
            if (userId != null) {
                // Quando un tutor viene valutato, rimuovilo dalla lista e aggiorna la valutazione
                homeViewModel.rateTutorAndRemoveFromList(userId, tutor, rating)
            }
        }
        recyclerView.adapter = adapter

        // Osserva i riferimenti dei tutor nel ViewModel
        homeViewModel.tutorRefs.observe(viewLifecycleOwner) { tutorRefs ->
            // Quando cambia la lista di riferimenti ai tutor, carica i tutor
            homeViewModel.loadTutors()
        }

        // Osserva i dati dei tutor e aggiorna la RecyclerView quando la lista cambia
        homeViewModel.tutors.observe(viewLifecycleOwner) { tutors ->
            adapter.updateData(tutors)
        }

        // Osserva i messaggi dal ViewModel per mostrare i Toast
        homeViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Carica l'utente e i riferimenti ai tutor da valutare se l'userId è presente
        if (userId != null) {
            // Ottiene l'utente da Firestore tramite FirebaseUtil
            homeViewModel.getListaTutorDaValutare(userId)
        }
    }
}
