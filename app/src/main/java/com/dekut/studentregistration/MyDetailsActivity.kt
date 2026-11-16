package com.dekut.studentregistration

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.utils.PdfGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MyDetailsActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var layoutContent: LinearLayout
    private lateinit var tvName: TextView
    private lateinit var tvIdNumber: TextView
    private lateinit var tvRegNumber: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvCourse: TextView
    private lateinit var tvDepartment: TextView
    private lateinit var tvRegStatus: TextView
    private lateinit var tvPaymentStatus: TextView
    private lateinit var tvDateRegistered: TextView
    private lateinit var btnPrint: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRepo: FirebaseRepository
    private lateinit var pdfGenerator: PdfGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_details)

        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()
        pdfGenerator = PdfGenerator(this)

        initializeViews()
        loadMyDetails()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progressBar)
        layoutContent = findViewById(R.id.layoutContent)
        tvName = findViewById(R.id.tvName)
        tvIdNumber = findViewById(R.id.tvIdNumber)
        tvRegNumber = findViewById(R.id.tvRegNumber)
        tvGender = findViewById(R.id.tvGender)
        tvCourse = findViewById(R.id.tvCourse)
        tvDepartment = findViewById(R.id.tvDepartment)
        tvRegStatus = findViewById(R.id.tvRegStatus)
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus)
        tvDateRegistered = findViewById(R.id.tvDateRegistered)
        btnPrint = findViewById(R.id.btnPrint)
    }

    private fun loadMyDetails() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            showError("Not logged in. Please log in again.")
            return
        }

        progressBar.visibility = View.VISIBLE
        layoutContent.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val student = firebaseRepo.getStudentByUid(uid)
                if (student == null) {
                    progressBar.visibility = View.GONE
                    showError("Could not find your registration details.")
                    return@launch
                }

                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

                tvName.text = student.fullName
                tvIdNumber.text = student.idNumber
                tvRegNumber.text = student.registrationNumber
                tvGender.text = student.gender
                tvCourse.text = student.course
                tvDepartment.text = student.department
                tvRegStatus.text = student.registrationStatus
                tvPaymentStatus.text = student.paymentStatus
                tvDateRegistered.text = sdf.format(java.util.Date(student.dateRegistered))

                progressBar.visibility = View.GONE
                layoutContent.visibility = View.VISIBLE

                btnPrint.setOnClickListener {
                    val html = pdfGenerator.generateStudentSummaryHtml(student, emptyList(), null, emptyList())
                    pdfGenerator.printHtmlContent(html, "My_Registration_${student.registrationNumber}")
                }

            } catch (e: Exception) {
                Log.e("MyDetailsActivity", "Error loading details", e)
                progressBar.visibility = View.GONE
                showError("Error: ${e.message}")
            }
        }
    }

    private fun showError(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.status_error)).show()
    }
}
