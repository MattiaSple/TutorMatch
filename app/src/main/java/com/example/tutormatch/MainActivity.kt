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
        tutor.setOnClickListener{
            intent = Intent(this,SecondActivity::class.java)
            startActivity(intent)
        }
    }
}