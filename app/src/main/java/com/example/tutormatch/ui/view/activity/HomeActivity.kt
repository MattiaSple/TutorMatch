package com.example.tutormatch.ui.view.activity


import android.os.Bundle
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
        val userId = intent.getStringExtra("userId")!!
        val nome = intent.getStringExtra("nome")!!
        val cognome = intent.getStringExtra("cognome")!!

        if (ruolo) {
            bindingTutor = ActivityHomeTutorBinding.inflate(layoutInflater)
            setContentView(bindingTutor.root)
            setupBottomNavigationTutor(bindingTutor.navView, userId, nome, cognome, ruolo)
        } else {
            bindingStudente = ActivityHomeStudenteBinding.inflate(layoutInflater)
            setContentView(bindingStudente.root)
            setupBottomNavigationStudente(bindingStudente.navView, userId, nome, cognome, ruolo)
        }
    }

    private fun setupBottomNavigationTutor(navView: BottomNavigationView, userId: String, nome: String, cognome: String, ruolo: Boolean) {
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragmentTutor(), userId, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_prenotazione -> {
                    replaceFragment(PrenotazioniFragment(), userId, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_calendario_tutor -> {
                    replaceFragment(CalendarioFragment(), userId, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_chat_tutor -> {
                    replaceFragment(ChatFragment(), userId, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_profilo_tutor -> {
                    replaceFragment(ProfiloFragment(), userId, nome, cognome, ruolo)
                    true
                }
                else -> false
            }
        }
        // Imposta il fragment iniziale
        replaceFragment(HomeFragmentTutor(), userId, nome, cognome, ruolo)
        navView.selectedItemId = R.id.navigation_home
    }

    private fun setupBottomNavigationStudente(navView: BottomNavigationView, userId: String, nome: String, cognome: String, ruolo: Boolean) {
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragmentStudente(), userId, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_ricerca_tutor -> {
                    replaceFragment(RicercaTutorFragment(), userId, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_prenotazione -> {
                    replaceFragment(PrenotazioniFragment(), userId, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_chat -> {
                    replaceFragment(ChatFragment(), userId, nome, cognome, ruolo)
                    true
                }
                R.id.navigation_profilo -> {
                    replaceFragment(ProfiloFragment(), userId, nome, cognome, ruolo)
                    true
                }
                else -> false
            }
        }
        // Imposta il fragment iniziale
        replaceFragment(HomeFragmentStudente(), userId, nome, cognome, ruolo)
        navView.selectedItemId = R.id.navigation_home
    }

    fun replaceFragment(fragment: Fragment, userId: String, nome: String? = "", cognome: String? = "", ruolo: Boolean, annuncioId: String? = null) {
        // Ottieni il Fragment attualmente visualizzato
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)

        // Verifica se il Fragment corrente è diverso dal Fragment che si vuole mostrare
        if (currentFragment?.javaClass != fragment.javaClass) {
            // Esegui la sostituzione solo se il Fragment corrente è diverso da quello selezionato
            val bundle = Bundle().apply {
                putString("userId", userId)
                nome?.let { putString("nome", it) }
                cognome?.let { putString("cognome", cognome) }
                putBoolean("ruolo", ruolo)
                annuncioId?.let { putString("annuncioId", it) }
            }
            fragment.arguments = bundle

            // Sostituisci il Fragment
            supportFragmentManager.commit {
                replace(R.id.nav_host_fragment_activity_main, fragment)
                // Rimuovi addToBackStack(null) se non hai bisogno di gestire il back stack
            }
        }
    }

    fun replaceFragmentChat(fragment: Fragment, chatId: String, email: String) {
        // Crea il bundle con i dati
        val bundle = Bundle().apply {
            putString("chatId", chatId)
            putString("email", email)
        }
        // Imposta il bundle nel fragment
        fragment.arguments = bundle

        // Avvia la transizione al nuovo fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp() || super.onSupportNavigateUp()
    }
}