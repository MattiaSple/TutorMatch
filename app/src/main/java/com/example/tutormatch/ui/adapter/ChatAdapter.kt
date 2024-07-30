package com.example.tutormatch.ui.adapter

class ChatAdapter {
}
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.tutormatch.R
//import com.example.tutormatch.data.source.Chat
//import kotlinx.android.synthetic.main.item_chat.view.*
//
//class ChatAdapter(
//    private var chats: List<Chat>,
//    private val onChatClick: (Chat) -> Unit
//) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
//
//    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun bind(chat: Chat) {
//            itemView.chat_tutor_name.text = chat.tutorName
//            itemView.chat_last_message.text = chat.lastMessage
//            itemView.chat_timestamp.text = chat.timestamp
//
//            itemView.setOnClickListener {
//                onChatClick(chat)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
//        return ChatViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
//        holder.bind(chats[position])
//    }
//
//    override fun getItemCount(): Int = chats.size
//
//    fun updateData(newChats: List<Chat>) {
//        chats = newChats
//        notifyDataSetChanged()
//    }
//}
