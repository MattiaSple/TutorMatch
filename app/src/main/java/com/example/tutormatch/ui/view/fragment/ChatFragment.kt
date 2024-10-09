package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentChatBinding
import com.example.tutormatch.ui.adapter.ChatAdapter
import com.example.tutormatch.ui.viewmodel.ChatViewModel
import com.example.tutormatch.ui.view.activity.HomeActivity
import com.example.tutormatch.util.FirebaseUtil

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        binding.viewModel = chatViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserFullName = "${arguments?.getString("nome")} ${arguments?.getString("cognome")}"
        val userId = arguments?.getString("userId") ?: ""

        binding.recyclerViewChat.layoutManager = LinearLayoutManager(context)

        FirebaseUtil.getUserFromFirestore(userId) { utente ->
            val email = utente?.email

            val adapter = email?.let {
                ChatAdapter(emptyList(), currentUserFullName, it, { chat ->
                    if (chat.id.isNullOrEmpty()) {
                    } else {
                        // Naviga al ChatDetailFragment passando chatId ed email
                        (activity as? HomeActivity)?.replaceFragmentChat(
                            ChatDetailFragment(),  // Il fragment di destinazione
                            chat.id,               // Passa il chatId
                            email                  // Passa l'email dell'utente
                        )
                    }
                }, { chatToDelete ->
                    // Mostra dialogo di conferma
                    AlertDialog.Builder(requireContext())
                        .setTitle("Elimina Chat")
                        .setMessage("Sei sicuro di voler eliminare questa chat?" +
                                "La conversazione sarà eliminata anche per l'altro partecipante")
                        .setPositiveButton("Sì") { _, _ ->
                            chatViewModel.deleteChat(chatToDelete.id) {
                                // Ricarica il fragment dopo l'eliminazione
                                chatViewModel.loadUserChats()
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
                })
            }

            binding.recyclerViewChat.adapter = adapter

            chatViewModel.chats.observe(viewLifecycleOwner) { chats ->
                adapter?.updateData(chats)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
