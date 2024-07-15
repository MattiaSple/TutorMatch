package com.example.tutormatch.ui.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.tutormatch.R
import com.example.tutormatch.databinding.ActivityHomeTutorBinding

class HomeTutorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityHomeTutorBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_home_tutor
        )

        // Recupera l'email dall'Intent
        val email = intent.getStringExtra("email")

    }
}
