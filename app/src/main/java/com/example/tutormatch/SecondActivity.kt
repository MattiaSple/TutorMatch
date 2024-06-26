package com.example.tutormatch

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.db.Utente

class SecondActivity : AppCompatActivity() {
    lateinit var viewModel: SecondActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrati)

        //viewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(SecondActivityViewModel::class.java)

        val isVerified = intent.getBooleanExtra("EXTRA_BOOLEAN", false)
        val nome = findViewById<EditText>(R.id.name)
        val cognome = findViewById<EditText>(R.id.surname)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val residenza = findViewById<EditText>(R.id.residenza)
        val via = findViewById<EditText>(R.id.via)
        val civico = findViewById<EditText>(R.id.civico)
        val accedi = findViewById<Button>(R.id.accedi)

        accedi.setOnClickListener {
            val tutor = Utente(
                        email.text.toString(),
                        nome.text.toString(),
                        cognome.text.toString(),
                        password.text.toString(),
                        residenza.text.toString(),
                        civico.text.toString(),
                        via.text.toString(),
                        isVerified
            )
            viewModel.insert(tutor)
            viewModel.leggiUtente("LAND")
        }
    }
}
