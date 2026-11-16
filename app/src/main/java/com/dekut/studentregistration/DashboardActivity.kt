package com.dekut.studentregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.repository.FirebaseRepository
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRepo: FirebaseRepository
    
    private lateinit var tvWelcome: TextView
    private lateinit var cardMyDetails: MaterialCardView
    private lateinit var cardLibrary: MaterialCardView
    private lateinit var cardHostel: MaterialCardView
    private lateinit var cardPayment: MaterialCardView
    private lateinit var cardAdminPanel: MaterialCardView
    private lateinit var cardLogout: MaterialCardView

    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            // Not logged in, go to welcome screen
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_dashboard)

        initializeViews()
        loadUserProfile()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        cardMyDetails = findViewById(R.id.cardMyDetails)
        cardLibrary = findViewById(R.id.cardLibrary)
        cardHostel = findViewById(R.id.cardHostel)
        cardPayment = findViewById(R.id.cardPayment)
        cardAdminPanel = findViewById(R.id.cardAdminPanel)
        cardLogout = findViewById(R.id.cardLogout)
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        
        lifecycleScope.launch {
            try {
                val student = firebaseRepo.getStudentByUid(uid)
                if (student != null) {
                    tvWelcome.text = "Welcome, ${student.fullName}!"
                    
                    // Check if admin
                    isAdmin = student.role == "ADMIN"
                    cardAdminPanel.visibility = if (isAdmin) View.VISIBLE else View.GONE
                    
                    Log.d("Dashboard", "User role: ${student.role}, isAdmin: $isAdmin")
                }
            } catch (e: Exception) {
                Log.e("Dashboard", "Error loading profile", e)
            }
        }
    }

    private fun setupClickListeners() {
        cardMyDetails.setOnClickListener {
            startActivity(Intent(this, MyDetailsActivity::class.java))
        }

        cardLibrary.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }

        cardHostel.setOnClickListener {
            startActivity(Intent(this, HostelActivity::class.java))
        }

        cardPayment.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        cardAdminPanel.setOnClickListener {
            if (isAdmin) {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Access Denied")
                    .setMessage("You do not have administrator privileges.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        cardLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
