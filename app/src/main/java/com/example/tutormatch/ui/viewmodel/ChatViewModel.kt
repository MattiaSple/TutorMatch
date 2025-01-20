package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.util.FirebaseUtil
import com.example.tutormatch.util.FirebaseUtil_Chat
import com.google.firebase.auth.FirebaseAuth

class ChatViewModel : ViewModel() {

    // LiveData per la lista delle chat dell'utente
    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> get() = _chats

    // LiveData per i messaggi relativi alla creazione delle chat
    private val _chatCreationMessage = MutableLiveData<String>()
    val chatCreationMessage: LiveData<String> get() = _chatCreationMessage

    init {
        // Carica le chat dell'utente al momento dell'inizializzazione del ViewModel
        loadUserChats()
    }

    // Recupera un utente specifico da Firestore utilizzando l'ID
    suspend fun getUser(userId: String): Utente? {
        return try {
            FirebaseUtil.getUserFromFirestore(userId) // Ottiene l'utente da Firestore
        } catch (e: Exception) {
            null // Ritorna null in caso di errore
        }
    }

    // Carica tutte le chat dell'utente corrente
    fun loadUserChats() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email // Ottiene l'email dell'utente corrente
        FirebaseUtil_Chat.loadUserChats(userEmail) { chatList ->
            _chats.value = chatList // Aggiorna la lista delle chat nel LiveData
        }
    }

    // Elimina una chat specifica utilizzando l'ID della chat
    fun deleteChat(chatId: String, onSuccess: () -> Unit) {
        FirebaseUtil_Chat.deleteChat(chatId, onSuccess) { error ->
            // Aggiorna il messaggio di errore in caso di fallimento
            _chatCreationMessage.value = "Errore durante l'eliminazione della chat: $error"
        }
    }

    // Crea una chat con un tutor
    fun creaChatConTutor(
        tutorEmail: String, // Email del tutor
        studenteEmail: String? = null, // Email dello studente (opzionale)
        tutorName: String, // Nome del tutor
        tutorSurname: String, // Cognome del tutor
        userName: String, // Nome dell'utente corrente
        userSurname: String, // Cognome dell'utente corrente
        materia: String, // Materia della chat
        onSuccess: (String) -> Unit, // Callback in caso di successo
        onFailure: (String) -> Unit, // Callback in caso di errore
        onConfirm: (String, () -> Unit) -> Unit // Callback per confermare la creazione di una nuova chat
    ) {
        val userEmail = studenteEmail ?: FirebaseAuth.getInstance().currentUser?.email ?: return
        // Controlla se esiste già una chat tra utente e tutor per la materia specifica
        FirebaseUtil_Chat.checkIfChatExists(userEmail, tutorEmail, materia, { exists, existingChatId ->
            if (exists && existingChatId != null) {
                // Se la chat esiste già, richiede una conferma prima di crearne una nuova
                onConfirm("Questa chat per questa materia già esiste. Sei sicuro di volerne creare un'altra?") {
                    creaNuovaChat(userEmail, tutorEmail, tutorName, tutorSurname, userName, userSurname, materia, onSuccess, onFailure)
                }
            } else {
                // Crea direttamente una nuova chat
                creaNuovaChat(userEmail, tutorEmail, tutorName, tutorSurname, userName, userSurname, materia, onSuccess, onFailure)
            }
        }, onFailure)
    }

    // Metodo privato per creare una nuova chat
    private fun creaNuovaChat(
        userEmail: String, // Email dell'utente corrente
        tutorEmail: String, // Email del tutor
        tutorName: String, // Nome del tutor
        tutorSurname: String, // Cognome del tutor
        userName: String, // Nome dell'utente corrente
        userSurname: String, // Cognome dell'utente corrente
        materia: String, // Materia della chat
        onSuccess: (String) -> Unit, // Callback in caso di successo
        onFailure: (String) -> Unit // Callback in caso di errore
    ) {
        // Crea un oggetto Chat con i dettagli
        val chatData = Chat(
            participants = listOf(userEmail, tutorEmail), // Partecipanti alla chat
            participantsNames = listOf("$userName $userSurname", "$tutorName $tutorSurname"), // Nomi dei partecipanti
            subject = materia, // Materia della chat
            lastMessage = null, // Ultimo messaggio (inizialmente null)
            messages = emptyMap() // Mappa vuota dei messaggi
        )
        // Salva la nuova chat nel database
        FirebaseUtil_Chat.createNewChat(chatData, onSuccess, onFailure)
    }
}
