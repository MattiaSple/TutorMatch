package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatDetailViewModel : ViewModel() {

    private val databaseUrl = "https://tutormatch-a7439-default-rtdb.europe-west1.firebasedatabase.app"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(databaseUrl)

    private lateinit var chatId: String
    private lateinit var messagesRef: DatabaseReference
    private lateinit var chatRef: DatabaseReference

    private val _chat = MutableLiveData<Chat?>()
    val chat: LiveData<Chat?> get() = _chat

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    val newMessage = MutableLiveData<String>()

    private var participants: List<String> = emptyList() // Partecipanti della chat

    // Imposta l'ID della chat e carica i dettagli della chat
    fun setChatId(chatId: String) {
        if (chatId.isBlank()) {
            return
        }

        this.chatId = chatId
        this.messagesRef = database.getReference("chats/$chatId/messages")
        this.chatRef = database.getReference("chats/$chatId")

        loadChatDetails()
        loadMessages()
    }

    private fun loadChatDetails() {
        if (!::chatId.isInitialized) {
            return
        }

        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chat = snapshot.getValue(Chat::class.java)
                _chat.value = chat
                participants = chat?.participants ?: emptyList()

                // Aggiorna `lastMessage.unreadBy` se contiene l'utente corrente
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                chat?.lastMessage?.let { lastMessage ->
                    if (currentUserEmail != null && lastMessage.unreadBy.contains(currentUserEmail)) {
                        val updatedUnreadBy = lastMessage.unreadBy.toMutableList().apply {
                            remove(currentUserEmail)
                        }
                        // Aggiorna `lastMessage.unreadBy` nel database
                        chatRef.child("lastMessage").child("unreadBy").setValue(updatedUnreadBy)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadMessages() {
        if (!::messagesRef.isInitialized) {
            return
        }

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let {
                        messagesList.add(it)
                    }
                }
                // Ordina i messaggi per timestamp, dal più vecchio al più recente
                messagesList.sortBy { it.timestamp }

                // Aggiorna il LiveData
                _messages.value = messagesList

                // Aggiorna lo stato di lettura dei messaggi per l'utente corrente
                markMessagesAsRead(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun markMessagesAsRead(messagesList: List<Message>) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        messagesList.forEach { message ->
            if (message.unreadBy.contains(currentUserEmail)) {
                // Rimuove l'utente corrente dalla lista di `unreadBy` nei messaggi
                val updatedUnreadBy = message.unreadBy.toMutableList().apply {
                    remove(currentUserEmail)
                }
                messagesRef.child(message.timestamp.toString()).child("unreadBy").setValue(updatedUnreadBy)
            }
        }
    }

    fun sendMessage() {
        if (!::chatId.isInitialized || !::messagesRef.isInitialized) {
            return
        }

        val messageText = newMessage.value
        if (messageText.isNullOrBlank()) {
            return
        }

        val senderEmail = FirebaseAuth.getInstance().currentUser?.email
        if (senderEmail.isNullOrBlank()) {
            return
        }

        if (participants.isEmpty()) {
            return
        }

        val newMessageId = messagesRef.push().key ?: "message_${System.currentTimeMillis()}"
        val message = mapOf(
            "senderId" to senderEmail,
            "text" to messageText,
            "timestamp" to ServerValue.TIMESTAMP,
            "unreadBy" to participants.filter { it != senderEmail }  // Aggiunge tutti i partecipanti tranne il mittente
        )

        // Aggiungi il messaggio al database della chat
        messagesRef.child(newMessageId).setValue(message).addOnSuccessListener {
            chatRef.child("lastMessage").setValue(message)
        }.addOnFailureListener {
        }

        newMessage.value = ""
    }
}
