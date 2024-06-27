package com.example.tutormatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import com.example.tutormatch.db.Utente
import androidx.lifecycle.Observer

class SecondActivity : AppCompatActivity() {
    lateinit var viewModel: SecondActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrati)

        viewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(SecondActivityViewModel::class.java)

        val isVerified = intent.getBooleanExtra("EXTRA_BOOLEAN", false)
        val nome = findViewById<EditText>(R.id.name)
        val cognome = findViewById<EditText>(R.id.surname)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val residenza = findViewById<EditText>(R.id.residenza)
        val via = findViewById<EditText>(R.id.via)
        val civico = findViewById<EditText>(R.id.civico)
        val accedi = findViewById<Button>(R.id.accedi)
        val back = findViewById<Button>(R.id.back)
        val studentObserver = Observer<Utente> {newValue ->
            Log.d("MainActivity__db","${newValue.nome} ${newValue.email} ${newValue.cognome}")
        }
        viewModel.utente.observe(this,studentObserver)

        //Osservo modifiche alla variabile studentList
        val studentListObserver = Observer<List<Utente>> {
            for(stud in it)
                Log.d("MainActivity__db","${stud.nome} ${stud.email} ${stud.cognome}")
        }
        viewModel.listaUtente.observe(this,studentListObserver)
        viewModel.leggiTuttiUtenti()
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
        back.setOnClickListener{val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)}


    }
}
