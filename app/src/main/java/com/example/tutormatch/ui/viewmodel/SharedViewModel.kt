package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _chatId = MutableLiveData<String>()
    val chatId: LiveData<String> get() = _chatId

    fun setChatId(id: String) {
        _chatId.value = id
    }
}