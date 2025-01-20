package com.example.tutormatch.util

import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Message
import com.google.firebase.database.*

object FirebaseUtil_Chat {

    private val databaseUrl = "https://tutormatch-a7439-default-rtdb.europe-west1.firebasedatabase.app" // URL del database Firebase
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(databaseUrl) // Istanza del database Firebase
    private val chatRef: DatabaseReference = database.getReference("chats") // Riferimento alla collezione "chats"

    // Carica le chat dell'utente corrente
    fun loadUserChats(userEmail: String?, onChatLoaded: (List<Chat>) -> Unit) {
        val chatList = mutableListOf<Chat>() // Lista temporanea per memorizzare le chat

        // Ascolta le modifiche ai figli nella collezione "chats"
        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(Chat::class.java) // Recupera l'oggetto Chat dal database
                if (chat?.participants?.contains(userEmail) == true) {
                    chatList.add(chat)
                    onChatLoaded(chatList.toList()) // Aggiorna la lista delle chat
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat != null && chat.participants.contains(userEmail)) {
                    val index = chatList.indexOfFirst { it.id == chat.id }
                    if (index != -1) {
                        chatList[index] = chat // Aggiorna la chat modificata
                        onChatLoaded(chatList.toList())
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat != null) {
                    chatList.removeAll { it.id == chat.id } // Rimuove la chat eliminata
                    onChatLoaded(chatList.toList())
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Elimina una chat dal database
    fun deleteChat(chatId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        chatRef.child(chatId).removeValue()
            .addOnSuccessListener { onSuccess() } // Notifica il successo
            .addOnFailureListener { e -> onFailure(e.message ?: "Errore durante l'eliminazione della chat") }
    }

    // Controlla se una chat esiste già tra due utenti per una materia specifica
    fun checkIfChatExists(
        userEmail: String,
        tutorEmail: String,
        materia: String,
        onChatExists: (Boolean, String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        chatRef.orderByChild("participants").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var chatExists = false
                var existingChatId: String? = null

                for (chatSnapshot in snapshot.children) {
                    val chat = chatSnapshot.getValue(Chat::class.java)
                    if (chat?.participants?.containsAll(listOf(userEmail, tutorEmail)) == true &&
                        chat.subject == materia) {
                        chatExists = true
                        existingChatId = chat.id
                        break
                    }
                }
                onChatExists(chatExists, existingChatId) // Restituisce il risultato della verifica
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message) // Gestisce errori nella richiesta
            }
        })
    }

    // Crea una nuova chat
    fun createNewChat(
        chatData: Chat,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val chatId = chatRef.push().key ?: return // Genera un nuovo ID per la chat
        chatData.id = chatId
        chatRef.child(chatId).setValue(chatData)
            .addOnSuccessListener { onSuccess(chatId) } // Notifica il successo
            .addOnFailureListener { e -> onFailure(e.message ?: "Errore durante la creazione della chat") }
    }

    // Carica i dettagli di una chat
    fun loadChatDetails(chatId: String, onChatLoaded: (Chat?, List<String>) -> Unit) {
        chatRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chat = snapshot.getValue(Chat::class.java) // Recupera i dettagli della chat
                val participants = chat?.participants ?: emptyList() // Ottiene i partecipanti alla chat
                onChatLoaded(chat, participants)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Carica i messaggi di una chat
    fun loadMessages(chatId: String, onMessagesLoaded: (List<Message>) -> Unit) {
        chatRef.child("$chatId/messages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                onMessagesLoaded(messagesList) // Restituisce la lista dei messaggi
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Aggiorna i messaggi non letti per l'utente corrente
    fun updateUnreadBy(chat: Chat?, currentUserEmail: String) {
        chat?.lastMessage?.unreadBy?.let { unreadBy ->
            if (unreadBy.contains(currentUserEmail)) {
                val updatedUnreadBy = unreadBy.toMutableList().apply { remove(currentUserEmail) }
                chatRef.child(chat.id).child("lastMessage/unreadBy").setValue(updatedUnreadBy) // Aggiorna lo stato
            }
        }
    }

    // Segna i messaggi come letti dall'utente corrente
    fun markMessagesAsRead(chatId: String, messagesList: List<Message>, currentUserEmail: String) {
        messagesList.forEach { message ->
            if (message.unreadBy.contains(currentUserEmail)) {
                val updatedUnreadBy = message.unreadBy.toMutableList().apply { remove(currentUserEmail) }
                chatRef.child("$chatId/messages/${message.timestamp}/unreadBy").setValue(updatedUnreadBy) // Aggiorna lo stato
            }
        }
    }

    // Invia un nuovo messaggio
    fun sendMessage(chatId: String, messageText: String, senderEmail: String, onComplete: () -> Unit) {
        val messagesRef = chatRef.child("$chatId/messages")
        val newMessageId = messagesRef.push().key ?: "message_${System.currentTimeMillis()}"

        loadChatDetails(chatId) { chat, participants ->
            val unreadByEmail = chat!!.participants.filter { it != senderEmail } // Esclude il mittente

            val message = mapOf(
                "senderId" to senderEmail,
                "text" to messageText,
                "timestamp" to ServerValue.TIMESTAMP,
                "unreadBy" to unreadByEmail // Aggiunge i destinatari non letti
            )

            chatRef.child(chatId).child("lastMessage").setValue(message).addOnSuccessListener {
                messagesRef.child(newMessageId).setValue(message).addOnSuccessListener {
                    onComplete() // Notifica il completamento
                }.addOnFailureListener {
                    it.printStackTrace() // Gestisce errori nell'aggiunta del messaggio
                }
            }.addOnFailureListener {
                it.printStackTrace() // Gestisce errori nell'aggiornamento di lastMessage
            }
        }
    }
}
