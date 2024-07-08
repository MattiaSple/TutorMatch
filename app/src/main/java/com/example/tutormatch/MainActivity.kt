package com.example.tutormatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val tutor = findViewById<Button>(R.id.creaAccountTutor)
        val studente = findViewById<Button>(R.id.creaAccountStudente)
        val accedi = findViewById<Button>(R.id.accedi)

        tutor.setOnClickListener{
            navigateToSecondActivity(true)
        }
        studente.setOnClickListener{
            navigateToSecondActivity(false)
        }
        accedi.setOnClickListener{
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }
    private fun navigateToSecondActivity(type: Boolean){
        val intent = Intent(this, SecondActivity::class.java).apply {
            putExtra("EXTRA_BOOLEAN", type)
        }
        startActivity(intent)
    }
}