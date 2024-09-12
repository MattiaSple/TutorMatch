package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.databinding.FragmentChatDetailBinding
import com.example.tutormatch.ui.adapter.MessageAdapter
import com.example.tutormatch.ui.viewmodel.ChatDetailViewModel
import com.example.tutormatch.ui.viewmodel.SharedViewModel
import androidx.recyclerview.widget.LinearLayoutManager

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatDetailViewModel: ChatDetailViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatDetailViewModel = ViewModelProvider(this).get(ChatDetailViewModel::class.java)

        binding.viewModel = chatDetailViewModel
        binding.lifecycleOwner = this

        val adapter = MessageAdapter(emptyList())

        // Aggiungi il LayoutManager qui
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMessages.adapter = adapter

        sharedViewModel.chatId.observe(viewLifecycleOwner, Observer { chatId ->
            if (!chatId.isNullOrEmpty()) {
                chatDetailViewModel.setChatId(chatId)
            } else {
                Log.e("ChatDetailFragment", "Received chatId is null or empty")
            }
        })

        chatDetailViewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            Log.d("ChatDetailFragment", "Messages updated: ${messages.size} items")
            adapter.updateData(messages)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
