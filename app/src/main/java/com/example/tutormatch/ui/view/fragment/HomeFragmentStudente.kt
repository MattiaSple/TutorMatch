package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentHomeStudenteBinding
import com.example.tutormatch.ui.adapter.ValutaTutorAdapter
import com.example.tutormatch.ui.viewmodel.HomeViewModel
import com.example.tutormatch.util.FirebaseUtil

class HomeFragmentStudente : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var _binding: FragmentHomeStudenteBinding
    private val binding get() = _binding
    private lateinit var adapter: ValutaTutorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeStudenteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        binding.viewModel = homeViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Configura la RecyclerView
        val recyclerView = binding.recyclerViewTutors
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inizializza l'adattatore
        adapter = ValutaTutorAdapter(emptyList()) { tutor, rating ->
            // Quando un tutor viene valutato, rimuovilo dalla lista e aggiorna la valutazione
            val userId = arguments?.getString("userId")
            if (userId != null) {
                homeViewModel.rateTutorAndRemoveFromList(userId, tutor, rating)
            }
        }
        recyclerView.adapter = adapter

        // Osserva il LiveData tutorRefs e carica i tutor quando cambia
        homeViewModel.tutorRefs.observe(viewLifecycleOwner) { tutorRefs ->
            homeViewModel.loadTutors()
        }

        // Osserva il LiveData dei tutor e aggiorna la RecyclerView quando la lista dei tutor cambia
        homeViewModel.tutors.observe(viewLifecycleOwner) { tutors ->
            adapter.updateData(tutors)
        }

        // Carica l'utente e i riferimenti ai tutor
        val userId = arguments?.getString("userId")
        if (userId != null) {
            FirebaseUtil.getUserFromFirestore(userId) { utente ->
                val lista = utente?.tutorDaValutare
                lista?.let {
                    // Carica i riferimenti dei tutor nell'utente
                    homeViewModel.loadTutorRefs(it)
                }
            }
        }

        return root
    }
}
