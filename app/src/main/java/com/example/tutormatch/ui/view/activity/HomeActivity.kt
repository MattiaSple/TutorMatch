package com.example.tutormatch.ui.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tutormatch.R
import com.example.tutormatch.databinding.ActivityHomeStudenteBinding
import com.example.tutormatch.databinding.ActivityHomeTutorBinding
import com.example.tutormatch.ui.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.ViewModelProvider
import android.util.Log

class HomeActivity : AppCompatActivity() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var bindingStudente: ActivityHomeStudenteBinding
    private lateinit var bindingTutor: ActivityHomeTutorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(HomeViewModel::class.java)

        val ruolo = intent.getBooleanExtra("ruolo", false)
        val email = intent.getStringExtra("email")
        val nome = intent.getStringExtra("nome")
        val cognome = intent.getStringExtra("cognome")
        homeViewModel.setRuolo(ruolo)
        if (nome != null && cognome != null) {
            homeViewModel.setBenvenuto(nome, cognome)
        }
        if (ruolo) {
            bindingTutor = ActivityHomeTutorBinding.inflate(layoutInflater)
            setContentView(bindingTutor.root)
            setupBottomNavigation(bindingTutor.navView, R.navigation.mobile_navigation_tutor, ruolo)
        } else {
            bindingStudente = ActivityHomeStudenteBinding.inflate(layoutInflater)
            setContentView(bindingStudente.root)
            setupBottomNavigation(bindingStudente.navView, R.navigation.mobile_navigation_studente, ruolo)
        }
    }

    private fun setupBottomNavigation(navView: BottomNavigationView, navGraphId: Int, isTutor: Boolean) {
        val navController = try {
            findNavController(R.id.nav_host_fragment_activity_main)
        } catch (e: Exception) {
            Log.e("HomeActivity", "NavController non trovato", e)
            null
        }

        if (navController != null) {
            navView.setupWithNavController(navController)

            navView.menu.clear()
            if (isTutor) {
                navView.inflateMenu(R.menu.bottom_nav_menu_tutor)
            } else {
                navView.inflateMenu(R.menu.bottom_nav_menu_studente)
            }
            navController.setGraph(navGraphId)
            // Imposta l'elemento "Home" come selezionato inizialmente
            navView.selectedItemId = R.id.navigation_home
        } else {
            Log.e("HomeActivity", "NavController è null")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
