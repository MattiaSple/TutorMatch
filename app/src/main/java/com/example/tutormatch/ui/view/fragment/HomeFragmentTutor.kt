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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeTutorBinding.inflate(inflater, container, false)

        // Imposta il lifecycle owner e il ViewModel per il binding
        binding.lifecycleOwner = viewLifecycleOwner
        annunciViewModel = ViewModelProvider(this).get(AnnunciViewModel::class.java)
        binding.viewModel = annunciViewModel

        // Configura il RecyclerView
        setupRecyclerView()

        // Recupera l'ID dell'utente dal bundle
        val userId = arguments?.getString("userId")
        userId?.let {
            // Imposta il riferimento al tutor nel ViewModel
            val firestore = FirebaseFirestore.getInstance()
            val tutorRef = firestore.collection("utenti").document(it)
            annunciViewModel.setTutorReference(tutorRef)
        }

        // Imposta il listener per il pulsante Salva
        binding.buttonSalva.setOnClickListener {
            val materiaSpinner = binding.spinnerMateria.selectedItem as? String
            annunciViewModel.materia.value = materiaSpinner
            if (!annunciViewModel.salvaAnnuncio(userId.toString())) {
                Toast.makeText(context, "Errore: tutti i campi devono essere compilati", Toast.LENGTH_SHORT).show()
            }
        }

        // Osserva i cambiamenti nei dati degli annunci
        annunciViewModel.listaAnnunciTutor.observe(viewLifecycleOwner) { listaAnnunci ->
            annuncioAdapter.setAnnunci(listaAnnunci)
            annuncioAdapter.notifyDataSetChanged()
        }

        // Osserva i messaggi di errore o stato
        annunciViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        annuncioAdapter = AnnuncioAdapter { annuncio ->
            annunciViewModel.eliminaAnnuncio(annuncio)
        }
        binding.recyclerViewAnnunci.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAnnunci.adapter = annuncioAdapter
    }
}
