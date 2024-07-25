package com.example.tutormatch.ui.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tutormatch.R
import com.example.tutormatch.databinding.ActivityHomeStudenteBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log

class HomeStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeStudenteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeStudenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = try {
            findNavController(R.id.nav_host_fragment_activity_main)
        } catch (e: Exception) {
            Log.e("HomeStudentActivity", "NavController non trovato", e)
            null
        }

        if (navController != null) {
            // Configura la BottomNavigationView per funzionare con il NavController
            navView.setupWithNavController(navController)
        } else {
            // Log or show an error message
            Log.e("HomeStudentActivity", "NavController è null")
        }

        val email = intent.getStringExtra("email")

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
