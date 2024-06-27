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
import com.example.tutormatch.ui.home.HomeFragment
class SecondActivity : AppCompatActivity() {
    lateinit var viewModel: SecondActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SecondActivityViewModel::class.java)
        val extraBoolean = intent.getBooleanExtra("EXTRA_BOOLEAN", false)
        viewModel.setExtraBoolean(extraBoolean)
        viewModel.extraBoolean.observe(this, Observer { value ->
            if (value != null && value) {
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
                val utentObserver = Observer<Utente> {newValue ->
                    Log.d("MainActivity__db","${newValue.nome} ${newValue.email} ${newValue.cognome}")
                }
                viewModel.utente.observe(this,utentObserver)
                val utentListObserver = Observer<List<Utente>> {
                    for(ut in it)
                        Log.d("MainActivity__db","${ut.nome} ${ut.email} ${ut.cognome}")
                }
                viewModel.listaUtente.observe(this,utentListObserver)
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

            } else {
                setContentView(R.layout.activity_accedi)
                val email = findViewById<EditText>(R.id.email)
                val password = findViewById<EditText>(R.id.password)
                var loginSuccess = false
                val utentListObserver = Observer<List<Utente>> {
                    for(ut in it) {
                        if (email.text.toString() == ut.email && password.text.toString() == ut.password) {
                            loginSuccess = true
                        }
                    }
                }
                viewModel.listaUtente.observe(this,utentListObserver)
                viewModel.leggiTuttiUtenti()
                if (loginSuccess){
                    val intent = Intent(this, HomeFragment::class.java)
                    startActivity(intent)
                }
            }
        })
    }
}
