package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Message
import com.example.tutormatch.util.FirebaseUtil_Chat
import com.google.firebase.auth.FirebaseAuth

class ChatDetailViewModel : ViewModel() {

    private lateinit var chatId: String
    private val _chat = MutableLiveData<Chat?>()
    val chat: LiveData<Chat?> get() = _chat

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    val newMessage = MutableLiveData<String>()

    // Imposta l'ID della chat e carica i dettagli della chat
    fun setChatId(chatId: String) {
        if (chatId.isBlank()) return
        this.chatId = chatId

        FirebaseUtil_Chat.loadChatDetails(chatId) { chat, participants ->
            _chat.value = chat
            updateUnreadBy(chat, FirebaseAuth.getInstance().currentUser?.email)
        }

        FirebaseUtil_Chat.loadMessages(chatId) { messagesList ->
            _messages.value = messagesList.sortedBy { it.timestamp }
            markMessagesAsRead(messagesList)
        }
    }

    private fun updateUnreadBy(chat: Chat?, currentUserEmail: String?) {
        currentUserEmail?.let {
            FirebaseUtil_Chat.updateUnreadBy(chat, it)
        }
    }

    private fun markMessagesAsRead(messagesList: List<Message>) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        FirebaseUtil_Chat.markMessagesAsRead(chatId, messagesList, currentUserEmail)
    }

    fun sendMessage() {
        val messageText = newMessage.value
        val senderEmail = FirebaseAuth.getInstance().currentUser?.email
        if (!messageText.isNullOrBlank() && !senderEmail.isNullOrBlank()) {
            FirebaseUtil_Chat.sendMessage(chatId, messageText, senderEmail) {
                newMessage.value = ""
            }
        }
    }
}
