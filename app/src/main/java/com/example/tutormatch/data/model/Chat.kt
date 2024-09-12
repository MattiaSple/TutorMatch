package com.example.tutormatch.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Chat(
    var id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val messages: Map<String, Message> = emptyMap() // Aggiungi questo se vuoi mappare i messaggi
)

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}