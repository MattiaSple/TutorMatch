package com.example.tutormatch.ui.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tutormatch.R

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isVerified = intent.getBooleanExtra("EXTRA_BOOLEAN", false)
        if (isVerified)
        {
            setContentView(R.layout.activity_hometutor)
        }else{
            setContentView(R.layout.activity_homestudente)
            //val navController = this.findNavController(R.id.myNavHostFragment)
        }
    }
}