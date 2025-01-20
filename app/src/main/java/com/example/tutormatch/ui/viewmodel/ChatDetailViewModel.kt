package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Message
import com.example.tutormatch.util.FirebaseUtil_Chat
import com.google.firebase.auth.FirebaseAuth

class ChatDetailViewModel : ViewModel() {

    private lateinit var chatId: String // ID della chat corrente
    private val _chat = MutableLiveData<Chat?>() // LiveData per i dettagli della chat
    val chat: LiveData<Chat?> get() = _chat

    private val _messages = MutableLiveData<List<Message>>() // LiveData per i messaggi della chat
    val messages: LiveData<List<Message>> get() = _messages

    val newMessage = MutableLiveData<String>() // Messaggio in fase di scrittura dall'utente

    // Imposta l'ID della chat e carica i dettagli della chat e i messaggi
    fun setChatId(chatId: String) {
        if (chatId.isBlank()) return // Se l'ID è vuoto, non fare nulla

        this.chatId = chatId

        // Carica i dettagli della chat (chat info e partecipanti)
        FirebaseUtil_Chat.loadChatDetails(chatId) { chat, participants ->
            _chat.value = chat
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
            currentUserEmail?.let {
                updateUnreadBy(chat, it) // Aggiorna i messaggi non letti per l'utente corrente
            }
        }

        // Carica i messaggi della chat ordinati per timestamp
        FirebaseUtil_Chat.loadMessages(chatId) { messagesList ->
            _messages.value = messagesList.sortedBy { it.timestamp }

            // Esegui le operazioni solo se i messaggi sono stati caricati correttamente
            if (messagesList.isNotEmpty()) {
                markMessagesAsRead(messagesList) // Segna i messaggi come letti
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                currentUserEmail?.let {
                    updateUnreadBy(_chat.value, it) // Aggiorna i messaggi non letti per l'utente corrente
                }
            }
        }
    }

    // Aggiorna i messaggi non letti per l'utente corrente
    private fun updateUnreadBy(chat: Chat?, currentUserEmail: String?) {
        currentUserEmail?.let {
            FirebaseUtil_Chat.updateUnreadBy(chat, it) // Aggiorna lo stato "unreadBy" su Firestore
        }
    }

    // Segna i messaggi come letti per l'utente corrente
    private fun markMessagesAsRead(messagesList: List<Message>) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        FirebaseUtil_Chat.markMessagesAsRead(chatId, messagesList, currentUserEmail) // Aggiorna lo stato di lettura
    }

    // Invia un nuovo messaggio
    fun sendMessage() {
        val messageText = newMessage.value // Testo del messaggio da inviare
        val senderEmail = FirebaseAuth.getInstance().currentUser?.email // Email del mittente
        if (!messageText.isNullOrBlank() && !senderEmail.isNullOrBlank()) {
            // Invia il messaggio tramite FirebaseUtil_Chat
            FirebaseUtil_Chat.sendMessage(chatId, messageText, senderEmail) {
                newMessage.value = "" // Resetta il campo di input dopo l'invio
            }
        }
    }
}
