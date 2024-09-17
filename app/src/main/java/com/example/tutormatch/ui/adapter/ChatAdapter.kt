package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.databinding.ItemChatBinding

class ChatAdapter(
    private var chats: List<Chat>,
    private val currentUserName: String,
    private val onClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    fun updateData(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int = chats.size

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            // Filtriamo i nomi dei partecipanti escludendo l'utente corrente
            val filteredParticipantsNames = chat.participantsNames.filter { participantName ->
                participantName != currentUserName
            }

            // Creiamo una rappresentazione semplificata del Chat con i partecipanti filtrati
            val filteredChat = chat.copy(participantsNames = filteredParticipantsNames)

            // Passiamo il chat filtrato al layout
            binding.chat = filteredChat

            // Gestisci il click sulla chat
            binding.root.setOnClickListener {
                onClick(chat)
            }
            binding.executePendingBindings()
        }
    }
}

