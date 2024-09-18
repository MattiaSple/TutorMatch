package com.example.tutormatch.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Message
import com.example.tutormatch.util.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatDetailViewModel : ViewModel() {

    private val databaseUrl = "https://tutormatch-a7439-default-rtdb.europe-west1.firebasedatabase.app"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(databaseUrl)

    private lateinit var chatId: String
    private lateinit var messagesRef: DatabaseReference

    private val _chat = MutableLiveData<Chat?>()
    val chat: LiveData<Chat?> get() = _chat

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    val newMessage = MutableLiveData<String>()

    // Imposta l'ID della chat e carica i dettagli della chat
    fun setChatId(chatId: String) {
        this.chatId = chatId
        this.messagesRef = database.getReference("chats/$chatId/messages")
        loadChatDetails()
        loadMessages()
    }

    // Carica i dettagli della chat
    private fun loadChatDetails() {
        val chatRef = database.getReference("chats/$chatId")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chat = snapshot.getValue(Chat::class.java)
                _chat.value = chat
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatDetailViewModel", "Errore nel caricamento dei dettagli della chat: ${error.message}")
            }
        })
    }

    private fun loadMessages() {
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let {
                        messagesList.add(it)
                    }
                }
                messagesList.sortBy { it.timestamp }
                _messages.value = messagesList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatDetailViewModel", "Errore nel caricamento dei messaggi: ${error.message}")
            }
        })
    }

    fun sendMessage() {
        if (!::chatId.isInitialized || !::messagesRef.isInitialized) {
            return
        }

        val messageText = newMessage.value ?: return
        val senderEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val timestamp = System.currentTimeMillis()

        val recipientEmail = getRecipientUserEmail() ?: return

        val newMessageId = "message_$timestamp"
        val message = Message(
            senderId = senderEmail,
            text = messageText,
            timestamp = timestamp
        )

        messagesRef.child(newMessageId).setValue(message).addOnSuccessListener {
            val chatRef = database.getReference("chats/$chatId")
            chatRef.child("lastMessage").setValue(message)
            FirebaseUtil.sendNotificationToUser(recipientEmail, messageText)
        }.addOnFailureListener {
            Log.e("ChatDetailViewModel", "Errore nell'invio del messaggio: ${it.message}")
        }

        newMessage.value = ""
    }

    // Recupera l'email del destinatario
    fun getRecipientUserEmail(): String? {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail == null) {
            Log.e("ChatDetailViewModel", "L'utente corrente non ha un'email associata.")
            return null
        }

        val participants = _chat.value?.participants
        return participants?.firstOrNull { it != currentUserEmail }
    }
}
