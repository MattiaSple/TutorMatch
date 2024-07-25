package com.example.tutormatch.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tutormatch.databinding.FragmentHomeStudenteBinding
import com.example.tutormatch.ui.viewmodel.HomeViewModel

class HomeFragmentStudente : Fragment() {

    private val homeViewModel: HomeViewModel by activityViewModels()
    private var _binding: FragmentHomeStudenteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeStudenteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.viewModel = homeViewModel
        binding.lifecycleOwner = viewLifecycleOwner



        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}