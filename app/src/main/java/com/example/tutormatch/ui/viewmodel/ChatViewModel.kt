package com.example.tutormatch.ui.viewmodel

import android.util.Log
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

    init {
        loadUserChats()
    }

    // Funzione per caricare le chat dell'utente corrente
    private fun loadUserChats() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = mutableListOf<Chat>()
                for (chatSnapshot in snapshot.children) {
                    val chat = chatSnapshot.getValue(Chat::class.java)
                    if (chat?.participants?.contains(userEmail) == true) {
                        chatList.add(chat)
                    }
                }
                _chats.value = chatList
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // Funzione per creare una chat tra l'utente attuale e il tutor, con nome, cognome e materia
    fun creaChatConTutor(
        tutorEmail: String,
        tutorName: String,
        tutorSurname: String,
        userName: String,
        userSurname: String,
        materia: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: return

        // Controlla se esiste già una chat tra questi utenti
        chatRef.orderByChild("participants")
            .equalTo(listOf(userEmail, tutorEmail).sorted().joinToString(","))
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        // Se la chat non esiste, creala
                        val chatId = chatRef.push().key ?: return
                        val chatData = Chat(
                            id = chatId,
                            participants = listOf(userEmail, tutorEmail),
                            participantsNames = listOf("$userName $userSurname", "$tutorName $tutorSurname"),
                            subject = materia,
                            lastMessage = null,
                            messages = emptyMap()
                        )
                        chatRef.child(chatId).setValue(chatData)
                            .addOnSuccessListener {
                                onSuccess(chatId)  // Callback per segnalare il successo
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Errore durante la creazione della chat")
                            }
                    } else {
                        // Se la chat esiste già, restituisci il suo id
                        val existingChatId = snapshot.children.first().key ?: ""
                        onSuccess(existingChatId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

}
