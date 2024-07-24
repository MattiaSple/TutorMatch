package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tutormatch.data.model.Utente

class UtenteViewModel(application: Application) : AndroidViewModel(application) {

    private val _userData = MutableLiveData<Utente>()
    val userData: LiveData<Utente> get() = _userData

    fun setUserData(user: Utente) {
        _userData.value = user
    }

}