package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _ruolo = MutableLiveData<Boolean>()
    val ruolo: LiveData<Boolean> get() = _ruolo

    private val _saluto = MutableLiveData<String>()
    val saluto: LiveData<String> get() = _saluto

    fun setRuolo(isTutor: Boolean) {
        _ruolo.value = isTutor
        _saluto.value = if (isTutor) "Ruolo: Tutor" else "Ruolo: Studente"
    }
}
