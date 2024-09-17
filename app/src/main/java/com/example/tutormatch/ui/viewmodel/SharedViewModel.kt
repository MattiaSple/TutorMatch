package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _chatId = MutableLiveData<String>()
    val chatId: LiveData<String> get() = _chatId

    // Funzione per impostare il chatId
    fun setChatId(id: String) {
        _chatId.value = id
    }

}
