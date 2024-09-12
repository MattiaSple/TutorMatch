package com.example.tutormatch.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatDetailViewModel : ViewModel() {

    // Specifica l'URL del tuo database Firebase
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
        Log.d("ChatDetailViewModel", "Inizio caricamento messaggi...")

        // Utilizza ValueEventListener per caricare tutti i messaggi esistenti sotto il nodo corretto
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let {
                        messagesList.add(it)
                    }
                }
                _messages.value = messagesList
                Log.d("ChatDetailViewModel", "Caricati ${messagesList.size} messaggi")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatDetailViewModel", "Errore durante il caricamento dei messaggi: ${error.message}", error.toException())
            }
        })
    }

    fun sendMessage() {
        if (!::chatId.isInitialized || !::messagesRef.isInitialized) {
            Log.e("ChatDetailViewModel", "chatId or messagesRef not initialized")
            return
        }

        val messageText = newMessage.value ?: return
        val senderEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val timestamp = System.currentTimeMillis()

        // Genera un nuovo ID messaggio usando il timestamp
        val newMessageId = "message_$timestamp"

        val message = Message(
            senderId = senderEmail,
            text = messageText,
            timestamp = timestamp
        )
        Log.d("ChatDetailViewModel", "Invio del messaggio: $messageText")

        // Salva il messaggio sotto il nodo "messages" della chat specifica
        messagesRef.child(newMessageId).setValue(message).addOnSuccessListener {
            Log.d("ChatDetailViewModel", "Messaggio inviato con successo: $messageText")
        }.addOnFailureListener {
            Log.e("ChatDetailViewModel", "Errore durante l'invio del messaggio", it)
        }
        newMessage.value = ""  // Resetta il campo di testo dopo l'invio
    }
}
