package com.example.tutormatch.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentChatBinding
import com.example.tutormatch.ui.chat.ChatViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val adapter = ChatAdapter(emptyList()) { chat ->
//            // Gestisci il click sull'elemento della chat
//        }
//        binding.recyclerViewChat.layoutManager = LinearLayoutManager(context)
//        binding.recyclerViewChat.adapter = adapter
//
//        chatViewModel.chats.observe(viewLifecycleOwner, Observer { chats ->
//            adapter.updateData(chats)
//        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
