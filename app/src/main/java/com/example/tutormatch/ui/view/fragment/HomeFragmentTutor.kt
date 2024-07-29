package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.databinding.FragmentHomeTutorBinding
import com.example.tutormatch.ui.adapter.AnnuncioAdapter
import com.example.tutormatch.ui.viewmodel.AnnunciViewModel

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
        val email = arguments?.getString("email")
        email?.let {
            // Imposta l'email nel ViewModel
            annunciViewModel.setEmailTutor(it)
        }
        // Imposta il listener per il pulsante Salva
        binding.buttonSalva.setOnClickListener {
            val materiaSpinner = binding.spinnerMateria.selectedItem as? String
            annunciViewModel.materia.value = materiaSpinner

            // Recupera l'email dal bundle
            val email1 = arguments?.getString("email")
            email1?.let {
                // Imposta l'email nel ViewModel
                annunciViewModel.setEmailTutor(it)
            }

            if (annunciViewModel.salvaAnnuncio()) {
                Toast.makeText(context, "Annuncio salvato con successo", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }

        annunciViewModel.annunci.observe(viewLifecycleOwner) {
            annuncioAdapter.setAnnunci(it)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        annuncioAdapter = AnnuncioAdapter()
        binding.recyclerViewAnnunci.adapter = annuncioAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
