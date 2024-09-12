package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tutormatch.databinding.FragmentChatBinding
import com.example.tutormatch.ui.adapter.ChatAdapter
import com.example.tutormatch.ui.viewmodel.ChatViewModel
import com.example.tutormatch.ui.viewmodel.SharedViewModel
import com.example.tutormatch.ui.view.activity.HomeActivity
import androidx.recyclerview.widget.LinearLayoutManager

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.viewModel = chatViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Imposta il LayoutManager
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(context)

        val adapter = ChatAdapter(emptyList()) { chat ->
            Log.d("ChatFragment", "Chat ID: ${chat.id}")
            if (chat.id.isNullOrEmpty()) {
                Log.e("ChatFragment", "Chat ID is null or empty")
            } else {
                sharedViewModel.setChatId(chat.id)  // Set the chatId in SharedViewModel
                (activity as? HomeActivity)?.replaceFragment(
                    ChatDetailFragment(),
                    email = arguments?.getString("email") ?: "",
                    nome = arguments?.getString("nome") ?: "",
                    cognome = arguments?.getString("cognome") ?: "",
                    ruolo = arguments?.getBoolean("ruolo") ?: false
                )
            }
        }

        binding.recyclerViewChat.adapter = adapter

        chatViewModel.chats.observe(viewLifecycleOwner, Observer { chats ->
            adapter.updateData(chats)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
