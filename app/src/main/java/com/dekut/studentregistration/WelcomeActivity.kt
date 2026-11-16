package com.dekut.studentregistration

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnNewRegistration: MaterialButton
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        
        // Check if user is already logged in
        if (auth.currentUser != null) {
            // User is logged in, go to dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_welcome)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnNewRegistration = findViewById(R.id.btnNewRegistration)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun setupClickListeners() {
        btnNewRegistration.setOnClickListener {
            startActivity(Intent(this, StudentRegistrationActivity::class.java))
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
