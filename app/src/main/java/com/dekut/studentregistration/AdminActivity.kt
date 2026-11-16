package com.dekut.studentregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.repository.FirebaseRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var btnViewAllStudents: MaterialButton
    private lateinit var btnManageLibrary: MaterialButton
    private lateinit var btnPrintAll: MaterialButton
    private lateinit var tvAdminName: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRepo: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()

        initializeViews()
        verifyAdminAndSetup()
    }

    private fun initializeViews() {
        btnViewAllStudents = findViewById(R.id.btnViewAllStudents)
        btnManageLibrary = findViewById(R.id.btnManageLibrary)
        btnPrintAll = findViewById(R.id.btnPrintAll)
        tvAdminName = findViewById(R.id.tvAdminName)
    }

    private fun verifyAdminAndSetup() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            showError("Not logged in.")
            finish(); return
        }

        lifecycleScope.launch {
            try {
                val student = firebaseRepo.getStudentByUid(uid)
                if (student == null || student.role != "ADMIN") {
                    showError("Access denied. Administrator privileges required.")
                    finish(); return@launch
                }

                tvAdminName.text = "Admin: ${student.fullName}"

                btnViewAllStudents.setOnClickListener {
                    startActivity(Intent(this@AdminActivity, StudentsActivity::class.java))
                }

                btnManageLibrary.setOnClickListener {
                    startActivity(Intent(this@AdminActivity, AdminLibraryActivity::class.java))
                }

                btnPrintAll.setOnClickListener {
                    printAllStudents()
                }

            } catch (e: Exception) {
                Log.e("AdminActivity", "Error verifying admin", e)
                showError("Error verifying permissions: ${e.message}")
                finish()
            }
        }
    }

    private fun printAllStudents() {
        btnPrintAll.isEnabled = false
        btnPrintAll.text = "Generating..."
        
        lifecycleScope.launch {
            try {
                Log.d("AdminActivity", "Fetching all students for report...")
                val students = firebaseRepo.getAllStudents().first()
                
                if (students.isEmpty()) {
                    Log.w("AdminActivity", "No students found or permission denied")
                    showError("No students found. Verify you have admin access and Firebase rules are configured correctly.")
                    btnPrintAll.isEnabled = true
                    btnPrintAll.text = "Print All Students"
                    return@launch
                }
                
                Log.d("AdminActivity", "Found ${students.size} students, generating report...")
                // Build comprehensive HTML for all students
                val allHtml = buildAllStudentsHtml(students)
                val pdfGenerator = com.dekut.studentregistration.utils.PdfGenerator(this@AdminActivity)
                pdfGenerator.printHtmlContent(allHtml, "DeKUT_All_Students_Report")
                
            } catch (e: Exception) {
                Log.e("AdminActivity", "Print all error", e)
                showError("Error generating report: ${e.message}")
            } finally {
                btnPrintAll.isEnabled = true
                btnPrintAll.text = "Print All Students"
            }
        }
    }

    private suspend fun buildAllStudentsHtml(students: List<com.dekut.studentregistration.models.Student>): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val now = sdf.format(java.util.Date())
        val sb = StringBuilder()

        sb.append("""
            <!DOCTYPE html><html><head><meta charset="UTF-8">
            <style>
              body { font-family: Arial, sans-serif; margin: 20px; color: #333; font-size: 11pt; }
              h1 { color: #156504; text-align: center; font-size: 18pt; margin-bottom: 4px; }
              h2 { color: #156504; text-align: center; font-size: 13pt; margin-top: 0; }
              .meta { text-align: center; color: #555; margin-bottom: 20px; font-size: 10pt; }
              .student-block { border: 2px solid #156504; border-radius: 6px; padding: 14px; margin-bottom: 20px; page-break-inside: avoid; }
              .section-head { background: #156504; color: white; padding: 5px 10px; margin: 10px -14px; font-weight: bold; font-size: 10pt; }
              .field-row { display: flex; padding: 3px 0; border-bottom: 1px solid #eee; }
              .field-label { font-weight: bold; width: 180px; color: #444; font-size: 10pt; }
              .field-value { flex: 1; font-size: 10pt; }
              table { width: 100%; border-collapse: collapse; margin-top: 6px; font-size: 9pt; }
              th { background: #156504; color: white; padding: 5px; text-align: left; }
              td { border: 1px solid #ddd; padding: 4px 6px; }
              tr:nth-child(even) { background: #f9f9f9; }
              .status-ok { color: #2E7D32; font-weight: bold; }
              .status-bad { color: #D32F2F; font-weight: bold; }
              .no-data { color: #888; font-style: italic; font-size: 10pt; padding: 4px 0; }
              .page-footer { text-align: center; margin-top: 30px; font-size: 9pt; color: #888; border-top: 1px solid #ddd; padding-top: 10px; }
            </style></head><body>
            <h1>DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY</h1>
            <h2>Student Registration Report — All Students</h2>
            <div class="meta">Generated: $now &nbsp;|&nbsp; Total Students: ${students.size}</div>
        """.trimIndent())

        for ((index, student) in students.withIndex()) {
            val payments = try { firebaseRepo.getPaymentsByUid(student.uid).ifEmpty {
                firebaseRepo.getPaymentsByStudentId(student.registrationNumber)
            }} catch (_: Exception) { emptyList() }
            val hostelList = try { firebaseRepo.getHostelRequestsByUid(student.uid).ifEmpty {
                firebaseRepo.getHostelRequestsByStudentId(student.registrationNumber)
            }} catch (_: Exception) { emptyList() }
            val libraryList = try { firebaseRepo.getLibraryRequestsByUid(student.uid).ifEmpty {
                firebaseRepo.getLibraryRequestsByStudentId(student.registrationNumber)
            }} catch (_: Exception) { emptyList() }

            val successPayments = payments.filter { it.paymentStatus == "SUCCESS" }
            val totalPaid = successPayments.sumOf { it.amount }
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

            sb.append("""
                <div class="student-block">
                <strong style="color:#156504;font-size:12pt;">#${index+1}. ${student.fullName}</strong>
                <span style="float:right;font-size:10pt;color:#888;">${student.registrationNumber}</span>
                <div class="section-head">Personal &amp; Academic Information</div>
            """.trimIndent())

            fun row(label: String, value: String) = """<div class="field-row"><div class="field-label">$label</div><div class="field-value">$value</div></div>"""
            sb.append(row("Full Names", student.fullName))
            sb.append(row("National ID Number", student.idNumber))
            sb.append(row("Registration Number", student.registrationNumber))
            sb.append(row("Gender", student.gender))
            sb.append(row("Course", student.course))
            sb.append(row("Department", student.department))
            sb.append(row("Registration Status", "<span class='${if(student.registrationStatus=="CONFIRMED") "status-ok" else ""}'>${student.registrationStatus}</span>"))
            sb.append(row("Payment Status", "<span class='${if(student.paymentStatus=="PAID") "status-ok" else "status-bad"}'>${student.paymentStatus}</span>"))
            sb.append(row("Date Registered", dateFormat.format(java.util.Date(student.dateRegistered))))

            // Hostel
            sb.append("<div class='section-head'>Hostel Information</div>")
            val hostel = hostelList.firstOrNull()
            if (hostel != null) {
                sb.append(row("Hostel Location", hostel.hostelLocation))
                sb.append(row("Room Type", hostel.roomType))
                sb.append(row("Sharable", hostel.sharable))
                sb.append(row("Request Date", dateFormat.format(java.util.Date(hostel.requestDate))))
            } else sb.append("<div class='no-data'>No hostel request.</div>")

            // Library
            sb.append("<div class='section-head'>Library Book Requests</div>")
            if (libraryList.isNotEmpty()) {
                sb.append("<table><thead><tr><th>ISBN</th><th>Title</th><th>Author</th><th>Borrowed</th><th>Return</th></tr></thead><tbody>")
                for (lib in libraryList) {
                    sb.append("<tr><td>${lib.bookIsbn}</td><td>${lib.bookTitle}</td><td>${lib.author}</td>")
                    sb.append("<td>${dateFormat.format(java.util.Date(lib.dateBorrowed))}</td>")
                    sb.append("<td>${dateFormat.format(java.util.Date(lib.dateOfReturn))}</td></tr>")
                }
                sb.append("</tbody></table>")
            } else sb.append("<div class='no-data'>No library book requests.</div>")

            // Fees
            sb.append("<div class='section-head'>Fees &amp; Payment Summary</div>")
            if (student.totalFeesPayable > 0) {
                val balance = student.totalFeesPayable - totalPaid
                sb.append(row("Total Fees Payable", "KES ${String.format("%,.2f", student.totalFeesPayable)}"))
                sb.append(row("Total Successful Payments", "KES ${String.format("%,.2f", totalPaid)}"))
                sb.append(row("Outstanding Balance", "KES ${String.format("%,.2f", balance)}"))
            } else {
                sb.append("<div class='no-data'>Fee balance unavailable (not configured).</div>")
                if (totalPaid > 0) sb.append(row("Total Successful Payments", "KES ${String.format("%,.2f", totalPaid)}"))
            }

            if (payments.isNotEmpty()) {
                sb.append("<table><thead><tr><th>Date</th><th>Amount (KES)</th><th>Transaction Ref</th><th>Status</th></tr></thead><tbody>")
                for (p in payments) {
                    val statusClass = if (p.paymentStatus == "SUCCESS") "status-ok" else if (p.paymentStatus == "FAILED") "status-bad" else ""
                    sb.append("<tr><td>${dateFormat.format(java.util.Date(p.transactionDate))}</td>")
                    sb.append("<td>${String.format("%,.2f", p.amount)}</td><td>${p.transactionReference}</td>")
                    sb.append("<td><span class='$statusClass'>${p.paymentStatus}</span></td></tr>")
                }
                sb.append("</tbody></table>")
            }

            sb.append("</div>") // end student-block
        }

        sb.append("""
            <div class="page-footer">
              DeKUT Student Registration System &mdash; Confidential &mdash; Generated $now<br/>
              &copy; ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} Dedan Kimathi University of Technology
            </div>
            </body></html>
        """.trimIndent())

        return sb.toString()
    }

    private fun showError(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.status_error)).show()
    }
}
