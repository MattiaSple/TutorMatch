package com.example.tutormatch.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _ruolo = MutableLiveData<Boolean>()
    val ruolo: LiveData<Boolean> get() = _ruolo

    private val _nome = MutableLiveData<String>()
    val nome: LiveData<String>
        get() = _nome

    private val _email = MutableLiveData<String>()
    val email: LiveData<String>
        get() = _email

    private val _cognome = MutableLiveData<String>()
    val cognome: LiveData<String>
        get() = _cognome

    private val _saluto = MutableLiveData<String>()

    val saluto: LiveData<String> get() = _saluto


    fun caricaDati(ruolo: Boolean, nome: String, cognome: String, email: String)
    {
        _ruolo.value = ruolo
        _nome.value = nome
        _cognome.value = cognome
        _email.value = email
        _saluto.value = "Benvenuto $nome $cognome!"
    }

}
