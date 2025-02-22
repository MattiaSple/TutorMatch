package com.example.tutormatch.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Chat(
    var id: String = "",
    val participants: List<String> = emptyList(), // Email dei partecipanti
    val participantsNames: List<String> = emptyList(), // Nomi completi dei partecipanti
    val subject: String = "", // Materia di discussione,
    val lastMessage: Message? = null, // Ultimo messaggio inviato,
    val messages: Map<String, Message> = emptyMap() // Messaggi della chat
) {
    // Proprieta calcolata per verificare se ci sono messaggi non letti per l'utente attuale
    fun hasUnreadMessages(userEmail: String): Boolean {
        return lastMessage?.unreadBy?.contains(userEmail) ?: true
    }
}

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val unreadBy: List<String> = emptyList() // Lista degli utenti che non hanno ancora letto il messaggio
) {
    val formattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

