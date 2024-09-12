package com.example.tutormatch.ui.view.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.tutormatch.R
import com.example.tutormatch.databinding.ActivityHomeStudenteBinding
import com.example.tutormatch.databinding.ActivityHomeTutorBinding
import com.example.tutormatch.ui.view.fragment.*
import com.example.tutormatch.ui.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var bindingStudente: ActivityHomeStudenteBinding
    private lateinit var bindingTutor: ActivityHomeTutorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[HomeViewModel::class.java]

        val ruolo = intent.getBooleanExtra("ruolo", false)
        val email = intent.getStringExtra("email") ?: "default_email@example.com" // Fornisci un default o gestisci null
        val nome = intent.getStringExtra("nome") ?: "Nome non disponibile"
        val cognome = intent.getStringExtra("cognome") ?: "Cognome non disponibile"
        // Configura la vista in base al ruolo
        if (ruolo) {
            bindingTutor = ActivityHomeTutorBinding.inflate(layoutInflater)
            setContentView(bindingTutor.root)
            setupBottomNavigationTutor(bindingTutor.navView, email, nome, cognome, ruolo)
        } else {
            bindingStudente = ActivityHomeStudenteBinding.inflate(layoutInflater)
            setContentView(bindingStudente.root)
            setupBottomNavigationStudente(bindingStudente.navView, email, nome, cognome, ruolo)
        }
    }

    private fun setupBottomNavigationTutor(navView: BottomNavigationView, email: String, nome: String, cognome: String, ruolo: Boolean) {
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragmentTutor(), email, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_prenotazione_tutor -> {
                    replaceFragment(PrenotazioniFragment(), email, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_calendario_tutor -> {
                    replaceFragment(CalendarioFragment(), email, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_chat_tutor -> {
                    replaceFragment(ChatFragment(), email, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_profilo_tutor -> {
                    replaceFragment(ProfiloFragment(), email, nome, cognome, ruolo)
                    true
                }
                else -> false
            }
        }
        // Imposta il fragment iniziale
        replaceFragment(HomeFragmentTutor(), email, nome, cognome, ruolo)
        navView.selectedItemId = R.id.navigation_home
    }

    private fun setupBottomNavigationStudente(navView: BottomNavigationView, email: String, nome: String, cognome: String, ruolo: Boolean) {
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragmentStudente(), email, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_ricerca_tutor -> {
                    replaceFragment(RicercaTutorFragment(), email, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_studente -> {
                    replaceFragment(PrenotazioniFragment(), email, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_chat -> {
                    replaceFragment(ChatFragment(), email, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_profilo -> {
                    replaceFragment(ProfiloFragment(), email, nome, cognome, ruolo)
                    true
                }
                else -> false
            }
        }
        // Imposta il fragment iniziale
        replaceFragment(HomeFragmentStudente(), email, nome, cognome, ruolo)
        navView.selectedItemId = R.id.navigation_home
    }

    fun replaceFragment(fragment: Fragment, email: String, nome: String, cognome: String, ruolo: Boolean) {
        val bundle = Bundle().apply {
            putString("email", email)
            putString("nome", nome)
            putString("cognome", cognome)
            putBoolean("ruolo", ruolo)
        }
        fragment.arguments = bundle

        supportFragmentManager.commit {
            replace(R.id.nav_host_fragment_activity_main, fragment)
            addToBackStack(null)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp() || super.onSupportNavigateUp()
    }
}
