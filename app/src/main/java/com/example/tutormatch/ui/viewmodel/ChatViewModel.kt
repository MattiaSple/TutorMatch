
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
                        // Aggiungiamo una proprietà `hasUnreadMessages` se l'utente ha messaggi non letti
                        chat?.hasUnreadMessages = chat.messages.values.any { it.unreadBy.contains(userEmail) }
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
        onFailure: (String) -> Unit,
        onConfirm: (String, () -> Unit) -> Unit  // Aggiungi il callback per conferma
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: return

        // Controlla se esiste già una chat tra questi utenti con la stessa materia
        chatRef.orderByChild("participants")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var chatExists = false
                    var existingChatId: String? = null

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(Chat::class.java)

                        // Verifica se i partecipanti e la materia coincidono
                        if (chat?.participants?.containsAll(listOf(userEmail, tutorEmail)) == true &&
                            chat.subject == materia) {
                            chatExists = true
                            existingChatId = chat.id
                            break
                        }
                    }

                    if (chatExists && existingChatId != null) {
                        // Esiste già una chat con gli stessi partecipanti e materia
                        onConfirm("Questa chat per questa materia già esiste. Sei sicuro di volerne creare un'altra?") {
                            // L'utente ha confermato la creazione di una nuova chat
                            creaNuovaChat(userEmail, tutorEmail, tutorName, tutorSurname, userName, userSurname, materia, onSuccess, onFailure)
                        }
                    } else {
                        // Non esiste una chat con questi parametri, creane una nuova
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
            lastMessage = null,
            messages = emptyMap(),
            hasUnreadMessages = false  // Imposta a false di default
        )
        chatRef.child(chatId).setValue(chatData)
            .addOnSuccessListener {
                onSuccess(chatId)  // Callback per segnalare il successo
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Errore durante la creazione della chat")
            }
    }
}