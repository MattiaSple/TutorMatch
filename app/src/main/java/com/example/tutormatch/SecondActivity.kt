package com.example.tutormatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.tutormatch.db.Utente
import androidx.lifecycle.Observer
class SecondActivity : AppCompatActivity() {
    lateinit var viewModel: SecondActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SecondActivityViewModel::class.java)
        val extraExists = intent.hasExtra("EXTRA_BOOLEAN")
        if (extraExists){
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
                if (isVerified){
                    accedi.text = "Registrati come tutor"
                }else{
                    accedi.text = "Registrati come studente"
                }
                val back = findViewById<Button>(R.id.back)
                accedi.setOnClickListener {
                    if (email.text.toString().isNotEmpty() && nome.text.toString().isNotEmpty() && cognome.text.toString().isNotEmpty() && password.text.toString().isNotEmpty() &&
                        residenza.text.toString().isNotEmpty() && civico.text.toString().isNotEmpty() && via.text.toString().isNotEmpty() &&
                        email.text.toString().count { it == '@' } == 1 && email.text.toString().contains(".")) {
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
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
                back.setOnClickListener{val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)}
            }else{
                setContentView(R.layout.activity_accedi)
                val email = findViewById<EditText>(R.id.email)
                val password = findViewById<EditText>(R.id.password)
                var loginSuccess = false
                var ruolo = false
                val accedi = findViewById<Button>(R.id.accedi)
                val back = findViewById<Button>(R.id.back)
                accedi.setOnClickListener {
                    val utentListObserver = Observer<List<Utente>> {
                        for(ut in it) {
                            if (email.text.toString() == ut.email && password.text.toString() == ut.password) {
                                loginSuccess = true
                                ruolo = ut.ruolo
                                break
                            }
                        }
                    }
                    viewModel.listaUtente.observe(this,utentListObserver)
                    viewModel.leggiTuttiUtenti()
                    if (loginSuccess) {
                        val intent = Intent(this, ThirdActivity::class.java)
                        intent.putExtra("EXTRA_BOOLEAN", ruolo)
                        startActivity(intent)
                    }else{
                        AlertDialog.Builder(this).apply {
                            setTitle("Errore")
                            setMessage("Login fallito. Controlla le tue credenziali e riprova.")
                            setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            create()
                            show()
                        }
                    }
                }
                back.setOnClickListener{val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)}
            }
        }
    }
