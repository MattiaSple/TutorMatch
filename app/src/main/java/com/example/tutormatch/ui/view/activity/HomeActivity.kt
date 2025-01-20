package com.example.tutormatch.ui.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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

    // Variabile per il ViewModel
    private lateinit var homeViewModel: HomeViewModel
    // Variabili per il binding dell'interfaccia utente per tutor e studente
    private lateinit var bindingStudente: ActivityHomeStudenteBinding
    private lateinit var bindingTutor: ActivityHomeTutorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il ViewModel con AndroidViewModelFactory
        homeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[HomeViewModel::class.java]

        // Ottiene i dati passati tramite l'intent (ruolo, userId, nome, cognome)
        val ruolo = intent.getBooleanExtra("ruolo", false)
        val userId = intent.getStringExtra("userId")!!
        val nome = intent.getStringExtra("nome")!!
        val cognome = intent.getStringExtra("cognome")!!

        // Se il ruolo è tutor, inflaziona il layout per tutor e imposta il bottom navigation appropriato
        if (ruolo) {
            bindingTutor = ActivityHomeTutorBinding.inflate(layoutInflater)
            setContentView(bindingTutor.root)
            setupBottomNavigationTutor(bindingTutor.navView, userId, nome, cognome, ruolo)
        } else {
            // Se il ruolo è studente, inflaziona il layout per studente e imposta il bottom navigation appropriato
            bindingStudente = ActivityHomeStudenteBinding.inflate(layoutInflater)
            setContentView(bindingStudente.root)
            setupBottomNavigationStudente(bindingStudente.navView, userId, nome, cognome, ruolo)
        }
    }

    // Configura la bottom navigation per il tutor
    private fun setupBottomNavigationTutor(navView: BottomNavigationView, userId: String, nome: String, cognome: String, ruolo: Boolean) {
        // Gestisce la selezione dei vari item nella bottom navigation
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Sostituisce il fragment con HomeFragmentTutor quando si seleziona la home
                    replaceFragment(HomeFragmentTutor(), userId)
                    true
                }
                R.id.navigation_prenotazione -> {
                    replaceFragment(PrenotazioniFragment(), userId, ruolo = ruolo)
                    true
                }
                R.id.navigation_calendario_tutor -> {
                    replaceFragment(CalendarioFragment(), userId)
                    true
                }
                R.id.navigation_chat_tutor -> {
                    replaceFragment(ChatFragment(), userId, nome, cognome)
                    true
                }
                R.id.navigation_profilo_tutor -> {
                    replaceFragment(ProfiloFragment(), userId, ruolo = ruolo)
                    true
                }
                else -> false
            }
        }
        // Imposta il fragment iniziale come HomeFragmentTutor
        navView.selectedItemId = R.id.navigation_home
    }

    // Configura la bottom navigation per lo studente
    private fun setupBottomNavigationStudente(navView: BottomNavigationView, userId: String, nome: String, cognome: String, ruolo: Boolean) {
        // Gestisce la selezione dei vari item nella bottom navigation
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Sostituisce il fragment con HomeFragmentStudente quando si seleziona la home
                    replaceFragment(HomeFragmentStudente(), userId)
                    true
                }
                R.id.navigation_ricerca_tutor -> {
                    replaceFragment(RicercaTutorFragment(), userId, nome, cognome)
                    true
                }
                R.id.navigation_prenotazione -> {
                    replaceFragment(PrenotazioniFragment(), userId, ruolo = ruolo)
                    true
                }
                R.id.navigation_chat -> {
                    replaceFragment(ChatFragment(), userId, nome, cognome)
                    true
                }
                R.id.navigation_profilo -> {
                    replaceFragment(ProfiloFragment(), userId, ruolo = ruolo)
                    true
                }
                else -> false
            }
        }
        // Imposta il fragment iniziale come HomeFragmentStudente
        navView.selectedItemId = R.id.navigation_home
    }

    // Funzione per sostituire i fragment, passando parametri come userId, nome, cognome e ruolo
    fun replaceFragment(fragment: Fragment, userId: String, nome: String? = "", cognome: String? = "", ruolo: Boolean? = false, annuncioId: String? = null) {
        // Ottiene il fragment attualmente visualizzato
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
        // Pulisce la back stack se il fragment corrente è CalendarioPrenotazioneFragment o ChatDetailFragment
        if (currentFragment is ChatDetailFragment) {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        // Verifica se il fragment attuale è diverso dal fragment che si vuole visualizzare
        if (currentFragment?.javaClass != fragment.javaClass) {
            // Se il fragment è diverso, crea un bundle con i dati e li passa al fragment
            val bundle = Bundle().apply {
                putString("userId", userId)
                nome?.let { putString("nome", it) }
                cognome?.let { putString("cognome", cognome) }
                ruolo?.let{ putBoolean("ruolo", ruolo) }
                annuncioId?.let { putString("annuncioId", it) }
            }
            fragment.arguments = bundle

            // Aggiungi il fragment alla back stack solo se ruolo è true
//            val transaction = supportFragmentManager.beginTransaction()
//                .replace(R.id.nav_host_fragment_activity_main, fragment)
//
//            if (currentFragment is RicercaTutorFragment) {
//                // Aggiungi alla back stack solo se ruolo è true
//                transaction.addToBackStack(null)
//            }
//            transaction.commit()
            // Sostituisce il fragment visualizzato con il nuovo
            supportFragmentManager.commit {
                replace(R.id.nav_host_fragment_activity_main, fragment)
                // Non aggiunge alla back stack per evitare comportamenti non desiderati nel back button
            }
        }
    }

    // Funzione specifica per sostituire il fragment della chat, passando chatId ed email
    fun replaceFragmentChat(fragment: Fragment, chatId: String, email: String) {
        // Crea un bundle con chatId ed email e lo imposta nel fragment
        val bundle = Bundle().apply {
            putString("chatId", chatId)
            putString("email", email)
        }
        fragment.arguments = bundle

        // Avvia la transizione verso il nuovo fragment e lo aggiunge alla back stack
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Gestisce la navigazione quando viene premuto il tasto "indietro" dell'app (DA VEDERE)
//    override fun onSupportNavigateUp(): Boolean {
//        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp() || super.onSupportNavigateUp()
//    }

    override fun onDestroy() {
        super.onDestroy()
        // Libera i binding per evitare memory leak
        if (::bindingStudente.isInitialized) bindingStudente.unbind()
        if (::bindingTutor.isInitialized) bindingTutor.unbind()
    }

}