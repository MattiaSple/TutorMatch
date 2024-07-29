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
        val email = intent.getStringExtra("email") ?: ""
        val nome = intent.getStringExtra("nome") ?: ""
        val cognome = intent.getStringExtra("cognome") ?: ""

        homeViewModel.caricaDati(ruolo, nome, cognome, email)


        if (ruolo) {
            bindingTutor = ActivityHomeTutorBinding.inflate(layoutInflater)
            setContentView(bindingTutor.root)
            setupBottomNavigation(bindingTutor.navView, R.navigation.mobile_navigation_tutor, email)
        } else {
            bindingStudente = ActivityHomeStudenteBinding.inflate(layoutInflater)
            setContentView(bindingStudente.root)
            setupBottomNavigation(bindingStudente.navView, R.navigation.mobile_navigation_studente, email)
        }
    }

    private fun setupBottomNavigation(navView: BottomNavigationView, navGraphId: Int, email: String) {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        navView.menu.clear()
        if (navGraphId == R.navigation.mobile_navigation_tutor) {
            navView.inflateMenu(R.menu.bottom_nav_menu_tutor)
        } else {
            navView.inflateMenu(R.menu.bottom_nav_menu_studente)
        }

        // Log email value
        Log.d("HomeActivity", "Passing email: $email")

        val bundle = Bundle().apply {
            putString("email", email)
        }
        navController.setGraph(navGraphId, bundle)
        navView.selectedItemId = R.id.navigation_home
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
