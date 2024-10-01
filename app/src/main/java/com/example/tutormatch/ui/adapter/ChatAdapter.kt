package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.R
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.databinding.ItemChatBinding

class ChatAdapter(
    private var chats: List<Chat>,
    private val currentUserName: String,
    private val currentUserEmail: String,
    private val onClick: (Chat) -> Unit,
    private val onDeleteClick: (Chat) -> Unit // Nuova callback per l'eliminazione
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    fun updateData(newChats: List<Chat>) {
        // Calcola la differenza tra la vecchia lista e la nuova per aggiornare correttamente l'adapter
        val oldChats = chats
        chats = newChats

        // Notifica il cambiamento della lista in modo più specifico
        if (oldChats.size < newChats.size) {
            notifyItemRangeInserted(oldChats.size, newChats.size - oldChats.size)
        } else if (oldChats.size > newChats.size) {
            notifyItemRangeRemoved(newChats.size, oldChats.size - newChats.size)
        } else {
            notifyItemRangeChanged(0, newChats.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat, currentUserName, currentUserEmail)
    }

    override fun getItemCount(): Int = chats.size

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat, userName: String, userEmail: String) {
            val filteredParticipantsNames = chat.participantsNames.filter { it != userName }
            val filteredChat = chat.copy(participantsNames = filteredParticipantsNames)

            binding.chat = filteredChat

            // Controlla se ci sono messaggi nella chat
            if (chat.messages.isNotEmpty()) {
                // Cambia il colore solo se ci sono messaggi e se ci sono messaggi non letti
                if (chat.hasUnreadMessages(userEmail)) {
                    binding.root.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.coloreSecondario)
                    )
                } else {
                    binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                }
            } else {
                // Imposta il colore predefinito se non ci sono messaggi
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
            }

            binding.root.setOnClickListener {
                onClick(chat)
            }

            binding.buttonDeleteChat.setOnClickListener {
                onDeleteClick(chat)
            }

            binding.executePendingBindings()
        }
    }
}
