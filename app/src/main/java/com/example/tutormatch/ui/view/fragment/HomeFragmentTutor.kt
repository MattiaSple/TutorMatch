package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentHomeTutorBinding
import com.example.tutormatch.ui.adapter.AnnuncioAdapter
import com.example.tutormatch.ui.viewmodel.AnnunciViewModel
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragmentTutor : Fragment() {

    private var _binding: FragmentHomeTutorBinding? = null
    private val binding get() = _binding!!
    private lateinit var annunciViewModel: AnnunciViewModel
    private lateinit var annuncioAdapter: AnnuncioAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeTutorBinding.inflate(inflater, container, false).apply {
            viewModel = ViewModelProvider(this@HomeFragmentTutor).get(AnnunciViewModel::class.java)
            lifecycleOwner = viewLifecycleOwner
        }
        annunciViewModel = ViewModelProvider(this).get(AnnunciViewModel::class.java)
        setupRecyclerView()

        // Recupera l'email dal bundle
        val userId = arguments?.getString("userId")
        userId?.let {
            // Imposta l'email nel ViewModel
            val firestore = FirebaseFirestore.getInstance()
            val tutorRef = firestore.collection("utenti").document(it)
            annunciViewModel.setTutorReference(tutorRef)
        }

        // Imposta il listener per il pulsante Salva
        binding.buttonSalva.setOnClickListener {
            val materiaSpinner = binding.spinnerMateria.selectedItem as? String
            annunciViewModel.materia.value = materiaSpinner
            annunciViewModel.salvaAnnuncio()
        }

        annunciViewModel.annunci.observe(viewLifecycleOwner, Observer { annunci ->
            annuncioAdapter.setAnnunci(annunci)
        })

        annunciViewModel.message.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

    private fun setupRecyclerView() {
        annuncioAdapter = AnnuncioAdapter { annuncio ->
            annunciViewModel.eliminaAnnuncio(annuncio)
        }
        binding.recyclerViewAnnunci.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAnnunci.adapter = annuncioAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
