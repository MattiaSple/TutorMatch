package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _chatId = MutableLiveData<String>()
    val chatId: LiveData<String> get() = _chatId
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email
    // Funzione per impostare il chatId
    fun setChatId(id: String) {
        _chatId.value = id
    }
    fun setEmail(email: String){
        _email.value = email
    }

}
