
package com.example.tutormatch.ui.adapter
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.R
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.databinding.ItemChatBinding

class ChatAdapter(
    private var chats: List<Chat>,
    private val currentUserName: String,  // Aggiungi l'email dell'utente corrente
    private val currentUserEmail: String,
    private val onClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    // Funzione per aggiornare i dati
    fun updateData(newChats: List<Chat>) {
        chats = newChats
        notifyItemRangeChanged(0, newChats.size)  // Questo notifica che tutti gli elementi sono cambiati
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat, currentUserName, currentUserEmail)  // Passiamo l'email dell'utente corrente
    }

    override fun getItemCount(): Int = chats.size

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat, userName: String, userEmail: String) {
            // Filtra i nomi dei partecipanti escludendo l'utente corrente
            val filteredParticipantsNames = chat.participantsNames.filter { participantName ->
                participantName != userName
            }

            // Crea una rappresentazione della chat con i partecipanti filtrati
            val filteredChat = chat.copy(participantsNames = filteredParticipantsNames)
            val statusBarColor = ContextCompat.getColor(binding.root.context, R.color.statusBarColor)
            // Passa la chat filtrata al layout
            binding.chat = filteredChat

            // Cambia colore della chat se ci sono messaggi non letti
            if (chat.hasUnreadMessages(userEmail)) {
                binding.root.setBackgroundColor(statusBarColor)  // Evidenzia se ci sono messaggi non letti
            } else {
                binding.root.setBackgroundColor(Color.WHITE)  // Colore normale se non ci sono messaggi non letti
            }

            // Gestisce il click sull'elemento della chat
            binding.root.setOnClickListener {
                onClick(chat)
            }

            // Applica immediatamente le binding
            binding.executePendingBindings()
        }
    }
}

