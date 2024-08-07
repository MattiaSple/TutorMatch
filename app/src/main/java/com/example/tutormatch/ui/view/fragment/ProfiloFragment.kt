package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.databinding.FragmentProfiloBinding
import com.example.tutormatch.ui.viewmodel.ProfiloViewModel

class ProfiloFragment : Fragment() {

    private var _binding: FragmentProfiloBinding? = null
    private val binding get() = _binding!!
    private lateinit var profiloViewModel: ProfiloViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        profiloViewModel = ViewModelProvider(this).get(ProfiloViewModel::class.java)

        _binding = FragmentProfiloBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = profiloViewModel

        // Recupera l'ID dell'utente dal bundle
        val userId = arguments?.getString("userId")
        userId?.let {
            profiloViewModel.loadUserProfile(it)
        }

        // Imposta il listener per il pulsante Salva
        binding.salva.setOnClickListener {
            userId?.let {
                profiloViewModel.saveUserProfile(it)
            }
        }

        profiloViewModel.message.observe(viewLifecycleOwner, Observer { text ->
            text?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
