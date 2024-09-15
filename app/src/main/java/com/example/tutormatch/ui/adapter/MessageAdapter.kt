package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Message
import com.example.tutormatch.databinding.ItemMessageBinding
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private var messages: List<Message>
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    fun updateData(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            val isMine = message.senderId == currentUserEmail
            binding.message = message
            binding.isMine = isMine
            binding.executePendingBindings()
        }
    }
}
