package com.example.tutormatch.ui.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tutormatch.R
import com.example.tutormatch.databinding.ActivityHomeTutorBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log

// Definizione della classe HomeTutorActivity che estende AppCompatActivity
class HomeTutorActivity : AppCompatActivity() {

    // Dichiarazione della variabile binding per ActivityHomeTutorBinding, inizializzata più tardi
    private lateinit var binding: ActivityHomeTutorBinding

    // Metodo onCreate, chiamato alla creazione dell'Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializzazione del binding usando il layout inflater per ottenere l'istanza di ActivityHomeTutorBinding
        binding = ActivityHomeTutorBinding.inflate(layoutInflater)

        // Impostazione del layout dell'Activity usando il root del binding
        setContentView(binding.root)

        // Ottenimento della BottomNavigationView dal layout tramite binding
        val navView: BottomNavigationView = binding.navView

        // Tentativo di ottenere il NavController dal NavHostFragment specificato
        val navController = try {
            // Trova il NavController associato all'elemento con ID nav_host_fragment_activity_main
            findNavController(R.id.nav_host_fragment_activity_main)
        } catch (e: Exception) {
            // In caso di errore, logga l'eccezione con un messaggio di errore
            Log.e("HomeTutorActivity", "NavController non trovato", e)
            null
        }

        // Verifica se il NavController è stato trovato correttamente
        if (navController != null) {
            // Configura la BottomNavigationView per funzionare con il NavController
            navView.setupWithNavController(navController)
        } else {
            // Se il NavController è null, logga un messaggio di errore
            Log.e("HomeTutorActivity", "NavController è null")
        }
    }

    // Metodo onSupportNavigateUp, chiamato quando l'utente preme il pulsante di navigazione "su"
    override fun onSupportNavigateUp(): Boolean {
        // Trova il NavController associato all'elemento con ID nav_host_fragment_activity_main
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Tenta di navigare "su" nella gerarchia di navigazione, se possibile
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
