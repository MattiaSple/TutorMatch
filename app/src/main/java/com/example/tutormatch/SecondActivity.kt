package com.example.tutormatch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AlertDialog
import com.example.tutormatch.db.Utente
import androidx.lifecycle.Observer
import com.example.tutormatch.ui.lista.UtenteViewModel

class SecondActivity : AppCompatActivity() {
    lateinit var utenteViewModel: UtenteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Inizializzo un viewmodel che abbia anche un context, tramite un modelFactory
        utenteViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(UtenteViewModel::class.java)

        utenteViewModel.listaUtenti.observe(this, Observer { utenti ->
            utenti.forEach { utente ->
                Log.d("CentralDatabase", "Utente: ${utente.nome} ${utente.cognome} ${utente.ruolo} ${utente.email}")
            }
        })



        val extraExists = intent.hasExtra("EXTRA_BOOLEAN")
        if (extraExists) {
            setContentView(R.layout.activity_registrati)
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

            if (isVerified) {
                accedi.text = "Registrati come tutor"
            } else {
                accedi.text = "Registrati come studente"
            }

            accedi.setOnClickListener {
                if (email.text.toString().isNotEmpty() && nome.text.toString().isNotEmpty() && cognome.text.toString().isNotEmpty() &&
                    password.text.toString().isNotEmpty() && residenza.text.toString().isNotEmpty() && civico.text.toString().isNotEmpty() &&
                    via.text.toString().isNotEmpty() && email.text.toString().count { it == '@' } == 1 && email.text.toString().contains(".")) {

                    val utente = Utente(
                        email.text.toString(),
                        nome.text.toString(),
                        cognome.text.toString(),
                        password.text.toString(),
                        residenza.text.toString(),
                        civico.text.toString(),
                        via.text.toString(),
                        isVerified
                    )

                    utenteViewModel.insert(utente)
                    utenteViewModel.delete(utente)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Aggiungi un messaggio di errore per campi mancanti o email non valida
                }
            }

            back.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        } else {
            setContentView(R.layout.activity_accedi)
            val email = findViewById<EditText>(R.id.email)
            val password = findViewById<EditText>(R.id.password)
            val accedi = findViewById<Button>(R.id.accedi)
            val back = findViewById<Button>(R.id.back)

//            accedi.setOnClickListener {
//                utenteViewModel.getAllUtenti()
//                utenteViewModel.listaUtenti.observe(this, Observer { utenti ->
//                    var loginSuccess = false
//                    var ruolo = false
//                    for (utente in utenti) {
//                        if (email.text.toString() == utente.email && password.text.toString() == utente.password) {
//                            loginSuccess = true
//                            ruolo = utente.ruolo
//                            break
//                        }
//                    }
//
//                    if (loginSuccess) {
//                        val intent = Intent(this, ThirdActivity::class.java)
//                        intent.putExtra("EXTRA_BOOLEAN", ruolo)
//                        startActivity(intent)
//                    } else {
//                        AlertDialog.Builder(this).apply {
//                            setTitle("Errore")
//                            setMessage("Login fallito. Controlla le tue credenziali e riprova.")
//                            setPositiveButton("OK") { dialog, _ ->
//                                dialog.dismiss()
//                            }
//                            create()
//                            show()
//                        }
//                    }
//                })
//            }
//
//            back.setOnClickListener {
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//            }
        }
    }
}
