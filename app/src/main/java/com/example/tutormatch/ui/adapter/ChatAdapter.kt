package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.R
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.databinding.ItemChatBinding

class ChatAdapter(
    private var chats: List<Chat>, // Lista delle chat
    private val currentUserName: String, // Nome dell'utente attuale
    private val currentUserEmail: String, // Email dell'utente attuale
    private val onClick: (Chat) -> Unit, // Callback per il click su una chat
    private val onDeleteClick: (Chat) -> Unit // Callback per il click sul pulsante elimina
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    fun updateData(newChats: List<Chat>) {
        // Aggiorna la lista di chat con un'ottimizzazione delle notifiche
        val oldChats = chats
        chats = newChats

        // Notifica i cambiamenti in modo efficiente
        if (oldChats.size < newChats.size) {
            notifyItemRangeInserted(oldChats.size, newChats.size - oldChats.size)
        } else if (oldChats.size > newChats.size) {
            notifyItemRangeRemoved(newChats.size, oldChats.size - newChats.size)
        } else {
            notifyItemRangeChanged(0, newChats.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        // Crea un ViewHolder inflazionando il layout della chat
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        // Collega i dati della chat corrente al ViewHolder
        val chat = chats[position]
        holder.bind(chat, currentUserName, currentUserEmail)
    }

    override fun getItemCount(): Int = chats.size // Restituisce il numero di chat

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat, userName: String, userEmail: String) {
            // Filtra i nomi dei partecipanti escludendo l'utente attuale
            val filteredParticipantsNames = chat.participantsNames.filter { it != userName }
            val filteredChat = chat.copy(participantsNames = filteredParticipantsNames)

            binding.chat = filteredChat // Associa la chat filtrata al layout

            // Cambia il colore dello sfondo se ci sono messaggi non letti
            if (chat.messages.isNotEmpty() && chat.hasUnreadMessages(userEmail)) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.coloreSecondario)
                )
            } else {
                // Reset dello sfondo a un colore di default per evitare il riciclo errato
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.transparent)
                )
            }

            // Gestisce il click sull'intera chat
            binding.root.setOnClickListener {
                onClick(chat)
            }

            // Gestisce il click sul pulsante elimina
            binding.buttonDeleteChat.setOnClickListener {
                onDeleteClick(chat)
            }

            binding.executePendingBindings() // Esegue immediatamente il binding per aggiornare la UI
        }
    }
}
