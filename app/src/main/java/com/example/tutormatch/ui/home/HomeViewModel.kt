package com.example.tutormatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _ruolo = MutableLiveData<Boolean>()
    val ruolo: LiveData<Boolean>
        get() = _ruolo

    fun setRuolo(ruolo: Boolean) {
        _ruolo.value = ruolo
    }
}
