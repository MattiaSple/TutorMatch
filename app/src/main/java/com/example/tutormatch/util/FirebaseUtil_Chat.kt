package com.example.tutormatch.util

import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object FirebaseUtil_Chat {

    private val databaseUrl = "https://tutormatch-a7439-default-rtdb.europe-west1.firebasedatabase.app"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(databaseUrl)
    private val chatRef: DatabaseReference = database.getReference("chats")

    fun loadUserChats(userEmail: String?, onChatLoaded: (List<Chat>) -> Unit) {
        val chatList = mutableListOf<Chat>()

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat?.participants?.contains(userEmail) == true) {
                    chatList.add(chat)
                    onChatLoaded(chatList.toList())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat != null && chat.participants.contains(userEmail)) {
                    val index = chatList.indexOfFirst { it.id == chat.id }
                    if (index != -1) {
                        chatList[index] = chat
                        onChatLoaded(chatList.toList())
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat != null) {
                    chatList.removeAll { it.id == chat.id }
                    onChatLoaded(chatList.toList())
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun deleteChat(chatId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        chatRef.child(chatId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Errore durante l'eliminazione della chat") }
    }

    fun checkIfChatExists(
        userEmail: String,
        tutorEmail: String,
        materia: String,
        onChatExists: (Boolean, String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        chatRef.orderByChild("participants")
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                    onChatExists(chatExists, existingChatId)
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

    fun createNewChat(
        chatData: Chat,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val chatId = chatRef.push().key ?: return
        chatData.id = chatId
        chatRef.child(chatId).setValue(chatData)
            .addOnSuccessListener { onSuccess(chatId) }
            .addOnFailureListener { e -> onFailure(e.message ?: "Errore durante la creazione della chat") }
    }
    fun loadChatDetails(chatId: String, onChatLoaded: (Chat?, List<String>) -> Unit) {
        chatRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chat = snapshot.getValue(Chat::class.java)
                val participants = chat?.participants ?: emptyList()
                onChatLoaded(chat, participants)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadMessages(chatId: String, onMessagesLoaded: (List<Message>) -> Unit) {
        chatRef.child("$chatId/messages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                onMessagesLoaded(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun updateUnreadBy(chat: Chat?, currentUserEmail: String) {
        chat?.lastMessage?.unreadBy?.let { unreadBy ->
            if (unreadBy.contains(currentUserEmail)) {
                val updatedUnreadBy = unreadBy.toMutableList().apply { remove(currentUserEmail) }
                chatRef.child(chat.id).child("lastMessage/unreadBy").setValue(updatedUnreadBy)
            }
        }
    }

    fun markMessagesAsRead(chatId: String, messagesList: List<Message>, currentUserEmail: String) {
        messagesList.forEach { message ->
            if (message.unreadBy.contains(currentUserEmail)) {
                val updatedUnreadBy = message.unreadBy.toMutableList().apply { remove(currentUserEmail) }
                chatRef.child("$chatId/messages/${message.timestamp}/unreadBy").setValue(updatedUnreadBy)
            }
        }
    }

    fun sendMessage(chatId: String, messageText: String, senderEmail: String, onComplete: () -> Unit) {
        val messagesRef = chatRef.child("$chatId/messages")
        val newMessageId = messagesRef.push().key ?: "message_${System.currentTimeMillis()}"

        loadChatDetails(chatId) { chat, participants ->
            // Trova tutti i partecipanti escluso il mittente
            val unreadByEmail = chat!!.participants.filter { it != senderEmail }

            // Crea il messaggio
            val message = mapOf(
                "senderId" to senderEmail,
                "text" to messageText,
                "timestamp" to ServerValue.TIMESTAMP,
                "unreadBy" to unreadByEmail // Lista di email che non hanno letto il messaggio
            )

            // Aggiorna il campo lastMessage prima
            chatRef.child(chatId).child("lastMessage").setValue(message).addOnSuccessListener {
                // Poi aggiungi il messaggio alla lista
                messagesRef.child(newMessageId).setValue(message).addOnSuccessListener {
                    onComplete()
                }.addOnFailureListener {
                    // Gestisci eventuali errori nell'aggiunta del messaggio
                    it.printStackTrace()
                }
            }.addOnFailureListener {
                // Gestisci eventuali errori nell'aggiornamento di lastMessage
                it.printStackTrace()
            }
        }
    }

}

