package com.example.tutormatch.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatViewModel : ViewModel() {

    // Specifica l'URL del tuo database Firebase
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

                    // Verifica se l'email dell'utente è nei partecipanti
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
}