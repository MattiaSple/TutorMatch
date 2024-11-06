package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.util.FirebaseUtil_Chat
import com.google.firebase.auth.FirebaseAuth

class ChatViewModel : ViewModel() {

    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> get() = _chats

    private val _chatCreationMessage = MutableLiveData<String>()
    val chatCreationMessage: LiveData<String> get() = _chatCreationMessage

    init {
        loadUserChats()
    }

    fun loadUserChats() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        FirebaseUtil_Chat.loadUserChats(userEmail) { chatList ->
            _chats.value = chatList
        }
    }

    fun deleteChat(chatId: String, onSuccess: () -> Unit) {
        FirebaseUtil_Chat.deleteChat(chatId, onSuccess) {
            _chatCreationMessage.value = "Errore durante l'eliminazione della chat: $it"
        }
    }

    fun creaChatConTutor(
        tutorEmail: String,
        studenteEmail: String? = null,
        tutorName: String,
        tutorSurname: String,
        userName: String,
        userSurname: String,
        materia: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        onConfirm: (String, () -> Unit) -> Unit
    ) {
        val userEmail = studenteEmail ?: FirebaseAuth.getInstance().currentUser?.email ?: return
        FirebaseUtil_Chat.checkIfChatExists(userEmail, tutorEmail, materia, { exists, existingChatId ->
            if (exists && existingChatId != null) {
                onConfirm("Questa chat per questa materia già esiste. Sei sicuro di volerne creare un'altra?") {
                    creaNuovaChat(userEmail, tutorEmail, tutorName, tutorSurname, userName, userSurname, materia, onSuccess, onFailure)
                }
            } else {
                creaNuovaChat(userEmail, tutorEmail, tutorName, tutorSurname, userName, userSurname, materia, onSuccess, onFailure)
            }
        }, onFailure)
    }

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
        val chatData = Chat(
            participants = listOf(userEmail, tutorEmail),
            participantsNames = listOf("$userName $userSurname", "$tutorName $tutorSurname"),
            subject = materia,
            lastMessage = null,
            messages = emptyMap()
        )
        FirebaseUtil_Chat.createNewChat(chatData, onSuccess, onFailure)
    }
}
