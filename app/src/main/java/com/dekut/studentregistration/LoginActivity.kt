package com.dekut.studentregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.repository.FirebaseRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var etRegistrationNumber: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRepo: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etRegistrationNumber = findViewById(R.id.etRegistrationNumber)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            if (validateInput()) {
                loginStudent()
            }
        }
    }

    private fun validateInput(): Boolean {
        val regNumber = etRegistrationNumber.text.toString().trim()
        val password = etPassword.text.toString()

        when {
            regNumber.isEmpty() -> {
                etRegistrationNumber.error = "Registration number is required"
                etRegistrationNumber.requestFocus()
                return false
            }

            password.isEmpty() -> {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return false
            }

            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return false
            }
        }
        return true
    }

    private fun loginStudent() {
        val regNumber = etRegistrationNumber.text.toString().trim().uppercase()
        val password = etPassword.text.toString()

        btnLogin.isEnabled = false
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Create email from registration number for Firebase Auth
                // MUST use the SAME normalization as registration
                val email =
                    "${regNumber.lowercase().replace("/", ".").replace(" ", "")}@dekut.student"

                Log.d("LoginActivity", "Attempting login for regNumber: $regNumber")
                Log.d("LoginActivity", "Generated auth email: $email")

                // Sign in with Firebase Authentication
                auth.signInWithEmailAndPassword(email, password).await()

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    Log.d("LoginActivity", "Login successful, UID: ${currentUser.uid}")

                    // Navigate to dashboard
                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    showError("Login failed. Please try again.")
                    btnLogin.isEnabled = true
                    progressBar.visibility = View.GONE
                }

            } catch (e: Exception) {
                Log.e("LoginActivity", "Login error", e)

                val errorMessage = when {
                    e.message?.contains("no user record", ignoreCase = true) == true ->
                        "No account found with this registration number."

                    e.message?.contains("password is invalid", ignoreCase = true) == true ->
                        "Incorrect password. Please try again."

                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your internet connection."

                    else -> "Login failed: ${e.message}"
                }

                showError(errorMessage)
                btnLogin.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
            .setBackgroundTint(
                androidx.core.content.ContextCompat.getColor(
                    this,
                    android.R.color.holo_red_dark
                )
            )
            .show()
    }
}
