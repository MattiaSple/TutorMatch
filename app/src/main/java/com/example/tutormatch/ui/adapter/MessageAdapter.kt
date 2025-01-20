package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Message
import com.example.tutormatch.databinding.ItemMessageBinding
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private var messages: List<Message> // Lista di messaggi
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email // Email dell'utente attuale

    fun updateData(newMessages: List<Message>) {
        // Aggiorna la lista dei messaggi e notifica i cambiamenti
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Crea il ViewHolder inflazionando il layout del messaggio
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        // Collega i dati del messaggio corrente al ViewHolder
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size // Restituisce il numero di messaggi

    inner class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            // Determina se il messaggio appartiene all'utente attuale
            val isMine = message.senderId == currentUserEmail
            binding.message = message // Associa il messaggio al layout
            binding.isMine = isMine // Configura la visualizzazione in base al mittente
            binding.executePendingBindings() // Aggiorna immediatamente la UI
        }
    }
}
