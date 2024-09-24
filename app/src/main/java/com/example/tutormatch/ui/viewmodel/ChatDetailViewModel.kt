package com.example.tutormatch.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

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

    private var participants: List<String> = emptyList() // Aggiungi questa variabile a livello di classe

    // Imposta l'ID della chat e carica i dettagli della chat
    fun setChatId(chatId: String) {
        if (chatId.isBlank()) {
            Log.e("ChatDetailViewModel", "ID chat non valido")
            return
        }

        this.chatId = chatId
        this.messagesRef = database.getReference("chats/$chatId/messages")
        loadChatDetails()
        loadMessages()
    }

    private fun loadChatDetails() {
        if (!::chatId.isInitialized) {
            return
        }

        val chatRef = database.getReference("chats/$chatId")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chat = snapshot.getValue(Chat::class.java)
                _chat.value = chat
                // Memorizza i partecipanti della chat
                participants = chat?.participants ?: emptyList()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatDetailViewModel", "Errore nel caricamento dei dettagli della chat: ${error.message}")
            }
        })
    }

    private fun loadMessages() {
        if (!::messagesRef.isInitialized) {
            Log.e("ChatDetailViewModel", "Reference messaggi non inizializzata")
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
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (messageSnapshot in snapshot.children) {
                            val messageId = messageSnapshot.key
                            val unreadBy = messageSnapshot.child("unreadBy").value as? MutableList<String>
                            if (unreadBy?.contains(currentUserEmail) == true) {
                                // Rimuove l'utente corrente dalla lista di `unreadBy`
                                unreadBy.remove(currentUserEmail)
                                messagesRef.child(messageId!!).child("unreadBy").setValue(unreadBy)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatDetailViewModel", "Errore nel caricamento dei messaggi: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatDetailViewModel", "Errore nel caricamento dei messaggi: ${error.message}")
            }
        })
    }


    fun sendMessage() {
        if (!::chatId.isInitialized || !::messagesRef.isInitialized) {
            Log.e("ChatDetailViewModel", "Chat o messagesRef non inizializzati")
            return
        }

        val messageText = newMessage.value
        if (messageText.isNullOrBlank()) {
            Log.e("ChatDetailViewModel", "Messaggio vuoto, non inviato")
            return
        }

        val senderEmail = FirebaseAuth.getInstance().currentUser?.email
        if (senderEmail.isNullOrBlank()) {
            Log.e("ChatDetailViewModel", "Email mittente vuota, impossibile inviare messaggio")
            return
        }

        // Verifica se i partecipanti sono stati caricati
        if (participants.isEmpty()) {
            Log.e("ChatDetailViewModel", "Partecipanti non disponibili, impossibile inviare il messaggio")
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
            val chatRef = database.getReference("chats/$chatId")
            chatRef.child("lastMessage").setValue(message)
        }.addOnFailureListener {
            Log.e("ChatDetailViewModel", "Errore nell'invio del messaggio: ${it.message}")
        }

        newMessage.value = ""
    }

}
