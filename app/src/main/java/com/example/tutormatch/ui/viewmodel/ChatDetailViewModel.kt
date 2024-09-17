package com.example.tutormatch.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Message
import com.example.tutormatch.util.FirebaseUtil // Utilizziamo FirebaseUtil per le operazioni Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatDetailViewModel : ViewModel() {

    private val databaseUrl = "https://tutormatch-a7439-default-rtdb.europe-west1.firebasedatabase.app"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(databaseUrl)

    private lateinit var chatId: String
    private lateinit var messagesRef: DatabaseReference

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    val newMessage = MutableLiveData<String>()

    fun setChatId(chatId: String) {
        this.chatId = chatId
        this.messagesRef = database.getReference("chats/$chatId/messages")
        loadMessages()
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

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendMessage(recipientUserId: String) {
        if (!::chatId.isInitialized || !::messagesRef.isInitialized) {
            return
        }

        val messageText = newMessage.value ?: return
        val senderEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val timestamp = System.currentTimeMillis()

        val newMessageId = "message_$timestamp"
        val message = Message(
            senderId = senderEmail,
            text = messageText,
            timestamp = timestamp
        )

        messagesRef.child(newMessageId).setValue(message).addOnSuccessListener {
            val chatRef = database.getReference("chats/$chatId")
            chatRef.child("lastMessage").setValue(message)

            // Invia notifica all'utente usando FirebaseUtil
            FirebaseUtil.sendNotificationToUser(recipientUserId, messageText)
        }.addOnFailureListener {
            Log.e("ChatDetailViewModel", "Failed to send message: ${it.message}")
        }

        newMessage.value = ""
    }
}

