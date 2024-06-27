package com.example.tutormatch
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.lifecycle.Observer
class ThirdActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isVerified = intent.getBooleanExtra("EXTRA_BOOLEAN", false)
        if (isVerified)
        {
            setContentView(R.layout.activity_hometutor)
        }else{
            setContentView(R.layout.activity_homestudente)
        }
    }
}