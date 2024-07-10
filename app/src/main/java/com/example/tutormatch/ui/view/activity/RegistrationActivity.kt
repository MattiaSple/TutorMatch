package com.example.tutormatch.ui.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.R
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.databinding.ActivityRegistratiBinding
import com.example.tutormatch.ui.viewmodel.UtenteViewModel

class RegistrationActivity : AppCompatActivity() {
    lateinit var utenteViewModel: UtenteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ottieni il binding
        val binding: ActivityRegistratiBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_registrati
        )




        //Inizializzo un viewmodel che abbia anche un context, tramite un modelFactory
        utenteViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(UtenteViewModel::class.java)

        utenteViewModel.listaUtenti.observe(this, Observer { utenti ->
            utenti.forEach { utente ->
                Log.d(
                    "CentralDatabase",
                    "Utente: ${utente.nome} ${utente.cognome} ${utente.ruolo} ${utente.email}"
                )
            }
        })

        val extraExists = intent.hasExtra("EXTRA_BOOLEAN")


        if (extraExists) {
            setContentView(R.layout.activity_registrati)
            val isVerified = intent.getBooleanExtra("EXTRA_BOOLEAN", false)
            val nome = findViewById<EditText>(R.id.name)
            val cognome = findViewById<EditText>(R.id.cognome)
            val email = findViewById<EditText>(R.id.email)
            val password = findViewById<EditText>(R.id.password)
            val residenza = findViewById<EditText>(R.id.residenza)
            val via = findViewById<EditText>(R.id.via)
            val civico = findViewById<EditText>(R.id.civico)
            val accedi = findViewById<Button>(R.id.registrazione)
            val back = findViewById<Button>(R.id.back)

            if (extraExists) {
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
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    showToastWithDelay(this, accedi)
                }
            }



            back.setOnClickListener {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        } else {
            setContentView(R.layout.activity_accedi)
            val email = findViewById<EditText>(R.id.email)
            val password = findViewById<EditText>(R.id.password)
            val accedi = findViewById<Button>(R.id.registrazione)
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
//                        val intent = Intent(this, LoginActivity::class.java)
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
//                val intent = Intent(this, HomeActivity::class.java)
//                startActivity(intent)
//            }
        }
    }
}

private fun showToastWithDelay(context: Context, button: Button) {
    // Disabilita il bottone
    button.isEnabled = false

    // Mostra il Toast
    Toast.makeText(context, "RIEMPI I CAMPI DIO SANTO", Toast.LENGTH_SHORT).show()

    // Usa un Handler per riabilitare il bottone dopo il ritardo specificato
    Handler(Looper.getMainLooper()).postDelayed({
        button.isEnabled = true
    }, 2500)
}