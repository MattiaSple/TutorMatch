package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatViewModel : ViewModel() {

    private val databaseUrl = "https://tutormatch-a7439-default-rtdb.europe-west1.firebasedatabase.app"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(databaseUrl)
    private val chatRef: DatabaseReference = database.getReference("chats")

    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> get() = _chats

    // LiveData per il messaggio di successo o fallimento
    private val _chatCreationMessage = MutableLiveData<String>()
    val chatCreationMessage: LiveData<String> get() = _chatCreationMessage

    init {
        loadUserChats()
    }

    // Caricamento e aggiornamento in tempo reale delle chat dell'utente
    fun loadUserChats() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        val chatList = mutableListOf<Chat>()

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat?.participants?.contains(userEmail) == true) {
                    chatList.add(chat)
                    _chats.value = chatList.toList()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat != null && chat.participants.contains(userEmail)) {
                    val index = chatList.indexOfFirst { it.id == chat.id }
                    if (index != -1) {
                        chatList[index] = chat
                        _chats.value = chatList.toList()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat != null) {
                    chatList.removeAll { it.id == chat.id }
                    _chats.value = chatList.toList()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun deleteChat(chatId: String, onSuccess: () -> Unit) {
        chatRef.child(chatId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
            }
    }

    // Funzione per creare una chat con un tutor
    fun creaChatConTutor(
        tutorEmail: String,
        studenteEmail: String?="",
        tutorName: String,
        tutorSurname: String,
        userName: String,
        userSurname: String,
        materia: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        onConfirm: (String, () -> Unit) -> Unit  // Callback per conferma
    ) {
        val userEmail = if (studenteEmail.isNullOrEmpty()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.email ?: run {
                return
            }
        } else {
            studenteEmail
        }

        // Controlla se esiste già una chat con il tutor per la stessa materia
        chatRef.orderByChild("participants")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var chatExists = false
                    var existingChatId: String? = null

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(Chat::class.java)

                        // Controlla se i partecipanti e la materia coincidono
                        if (chat?.participants?.containsAll(listOf(userEmail, tutorEmail)) == true &&
                            chat.subject == materia) {
                            chatExists = true
                            existingChatId = chat.id
                            break
                        }
                    }

                    if (chatExists && existingChatId != null) {
                        // Esiste già una chat, chiedi conferma per crearne una nuova
                        onConfirm("Questa chat per questa materia già esiste. Sei sicuro di volerne creare un'altra?") {
                            creaNuovaChat(userEmail, tutorEmail, tutorName, tutorSurname, userName, userSurname, materia, onSuccess, onFailure)
                        }
                    } else {
                        // Crea una nuova chat
                        creaNuovaChat(userEmail, tutorEmail, tutorName, tutorSurname, userName, userSurname, materia, onSuccess, onFailure)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

    // Funzione per creare una nuova chat
    private fun creaNuovaChat(
        userEmail: String,
        tutorEmail: String,
        tutorName: String,
        tutorSurname: String,
        userName: String,
        userSurname: String,
        materia: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val chatId = chatRef.push().key ?: return
        val chatData = Chat(
            id = chatId,
            participants = listOf(userEmail, tutorEmail),
            participantsNames = listOf("$userName $userSurname", "$tutorName $tutorSurname"),
            subject = materia,
            lastMessage = null,  // Inizialmente nessun messaggio
            messages = emptyMap() // Messaggi vuoti all'inizio
        )
        chatRef.child(chatId).setValue(chatData)
            .addOnSuccessListener {
                _chatCreationMessage.value = "Chat creata con successo!"
                onSuccess(chatId)  // Callback di successo
            }
            .addOnFailureListener { e ->
                _chatCreationMessage.value = "Errore durante la creazione della chat: ${e.message}"
                onFailure(e.message ?: "Errore durante la creazione della chat")
            }
    }
}
