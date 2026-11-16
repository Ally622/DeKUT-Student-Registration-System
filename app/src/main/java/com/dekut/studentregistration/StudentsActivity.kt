package com.dekut.studentregistration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dekut.studentregistration.adapters.StudentsAdapter
import com.dekut.studentregistration.models.*
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.utils.PdfGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StudentsActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var btnAll: MaterialButton
    private lateinit var btnConfirmed: MaterialButton
    private lateinit var btnPrintAll: MaterialButton
    private lateinit var tvStudentCount: TextView
    private lateinit var rvStudents: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var firebaseRepo: FirebaseRepository
    private lateinit var pdfGenerator: PdfGenerator
    private lateinit var adapter: StudentsAdapter
    private lateinit var auth: FirebaseAuth

    private var allStudents = listOf<Student>()
    private var filteredStudents = listOf<Student>()
    private var currentFilter = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)

        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()
        pdfGenerator = PdfGenerator(this)

        // Server-side role check — double guard
        verifyAdminAccess()

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadStudents()
    }

    private fun verifyAdminAccess() {
        val uid = auth.currentUser?.uid ?: run { finish(); return }
        lifecycleScope.launch {
            val student = firebaseRepo.getStudentByUid(uid)
            if (student?.role != "ADMIN") {
                showError("Access denied.")
                finish()
            }
        }
    }

    private fun initializeViews() {
        etSearch = findViewById(R.id.etSearch)
        btnAll = findViewById(R.id.btnAll)
        btnConfirmed = findViewById(R.id.btnConfirmed)
        btnPrintAll = findViewById(R.id.btnPrintAll)
        tvStudentCount = findViewById(R.id.tvStudentCount)
        rvStudents = findViewById(R.id.rvStudents)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        adapter = StudentsAdapter(
            onStudentClick = { student -> showStudentDetails(student) },
            onPrintClick   = { student -> printStudentSummary(student) }
        )
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = adapter
    }

    private fun setupClickListeners() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterStudents(s.toString()) }
        })
        btnAll.setOnClickListener { currentFilter = "ALL"; updateFilterButtons(); applyFilter() }
        btnConfirmed.setOnClickListener { currentFilter = "CONFIRMED"; updateFilterButtons(); applyFilter() }
        btnPrintAll.setOnClickListener { printAllStudents() }
        updateFilterButtons()
    }

    private fun updateFilterButtons() {
        btnAll.setBackgroundColor(if (currentFilter == "ALL") getColor(R.color.dekut_red) else getColor(android.R.color.transparent))
        btnAll.setTextColor(if (currentFilter == "ALL") getColor(R.color.white) else getColor(R.color.dekut_red))
        btnConfirmed.setBackgroundColor(if (currentFilter == "CONFIRMED") getColor(R.color.dekut_red) else getColor(android.R.color.transparent))
        btnConfirmed.setTextColor(if (currentFilter == "CONFIRMED") getColor(R.color.white) else getColor(R.color.dekut_red))
    }

    private fun loadStudents() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                Log.d("StudentsActivity", "Loading all students...")
                firebaseRepo.getAllStudents()
                    .onEach { students ->
                        Log.d("StudentsActivity", "Received ${students.size} students")
                        
                        if (students.isEmpty()) {
                            // Could be permission denied or genuinely no students
                            Log.w("StudentsActivity", "Received empty student list")
                            allStudents = emptyList()
                            tvEmptyState.text = "No students found or access denied.\nVerify you have admin privileges."
                            tvEmptyState.visibility = View.VISIBLE
                        } else {
                            allStudents = students
                            tvEmptyState.text = "No students found"
                        }
                        
                        applyFilter()
                        updateStudentCount()
                        progressBar.visibility = View.GONE
                    }
                    .launchIn(this)
            } catch (e: Exception) {
                Log.e("StudentsActivity", "Error loading students", e)
                progressBar.visibility = View.GONE
                tvEmptyState.text = "Error loading students: ${e.message}"
                tvEmptyState.visibility = View.VISIBLE
                showError("Failed to load students. Check your permissions.")
            }
        }
    }

    private fun applyFilter() {
        filteredStudents = if (currentFilter == "CONFIRMED")
            allStudents.filter { it.registrationStatus == "CONFIRMED" }
        else allStudents
        filterStudents(etSearch.text.toString())
    }

    private fun filterStudents(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) filteredStudents
        else filteredStudents.filter {
            it.registrationNumber.lowercase().contains(q) ||
            it.idNumber.lowercase().contains(q) ||
            it.fullName.lowercase().contains(q)
        }
        adapter.submitList(filtered)
        rvStudents.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        tvEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        updateStudentCount()
    }

    private fun updateStudentCount() {
        val confirmed = allStudents.count { it.registrationStatus == "CONFIRMED" }
        tvStudentCount.text = "Total: ${allStudents.size} | Confirmed: $confirmed"
    }

    // ── Full student details dialog ──────────────────────────────────────────

    private fun showStudentDetails(student: Student) {
        lifecycleScope.launch {
            try {
                val uid = student.uid.ifEmpty { student.id }
                val payments = firebaseRepo.getPaymentsByUid(uid).ifEmpty {
                    firebaseRepo.getPaymentsByStudentId(student.registrationNumber)
                }
                val hostelList = firebaseRepo.getHostelRequestsByUid(uid).ifEmpty {
                    firebaseRepo.getHostelRequestsByStudentId(student.registrationNumber)
                }
                val libraryList = firebaseRepo.getLibraryRequestsByUid(uid).ifEmpty {
                    firebaseRepo.getLibraryRequestsByStudentId(student.registrationNumber)
                }
                val hostel = hostelList.firstOrNull()
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val successPaid = payments.filter { it.paymentStatus == "SUCCESS" }.sumOf { it.amount }

                val msg = buildString {
                    appendLine("━━ PERSONAL INFO ━━━━━━━━━━━━")
                    appendLine("Name:      ${student.fullName}")
                    appendLine("ID No:     ${student.idNumber}")
                    appendLine("Reg No:    ${student.registrationNumber}")
                    appendLine("Gender:    ${student.gender}")
                    appendLine("Course:    ${student.course}")
                    appendLine("Dept:      ${student.department}")
                    appendLine("Status:    ${student.registrationStatus}")
                    appendLine("Payment:   ${student.paymentStatus}")
                    appendLine("Registered:${sdf.format(Date(student.dateRegistered))}")
                    appendLine()
                    appendLine("━━ HOSTEL ━━━━━━━━━━━━━━━━━━━")
                    if (hostel != null) {
                        appendLine("Location: ${hostel.hostelLocation}")
                        appendLine("Room:     ${hostel.roomType}")
                        appendLine("Sharable: ${hostel.sharable}")
                    } else appendLine("No hostel request.")
                    appendLine()
                    appendLine("━━ LIBRARY (${libraryList.size} request(s)) ━━━━")
                    if (libraryList.isNotEmpty()) {
                        for ((i, lib) in libraryList.withIndex()) {
                            appendLine("${i+1}. ${lib.bookTitle}")
                            appendLine("   ISBN: ${lib.bookIsbn} | ${lib.author}")
                            appendLine("   Borrowed: ${sdf.format(Date(lib.dateBorrowed))} → ${sdf.format(Date(lib.dateOfReturn))}")
                        }
                    } else appendLine("No library book requests.")
                    appendLine()
                    appendLine("━━ FEES ━━━━━━━━━━━━━━━━━━━━━")
                    if (student.totalFeesPayable > 0) {
                        appendLine("Payable:   KES ${String.format("%,.2f", student.totalFeesPayable)}")
                        appendLine("Paid:      KES ${String.format("%,.2f", successPaid)}")
                        appendLine("Balance:   KES ${String.format("%,.2f", student.totalFeesPayable - successPaid)}")
                    } else {
                        appendLine("Fee balance unavailable.")
                        if (successPaid > 0)
                            appendLine("Paid so far: KES ${String.format("%,.2f", successPaid)}")
                    }
                    if (payments.isNotEmpty()) {
                        appendLine()
                        appendLine("Transactions:")
                        for (p in payments)
                            appendLine("  ${sdf.format(Date(p.transactionDate))} | KES ${String.format("%,.2f", p.amount)} | ${p.paymentStatus} | ${p.transactionReference}")
                    }
                }

                AlertDialog.Builder(this@StudentsActivity)
                    .setTitle("${student.fullName}")
                    .setMessage(msg)
                    .setPositiveButton("🖨️ Print") { _, _ -> printStudentSummary(student) }
                    .setNegativeButton("Close", null)
                    .show()

            } catch (e: Exception) {
                Log.e("StudentsActivity", "Error loading student details", e)
                showError("Error loading details: ${e.message}")
            }
        }
    }

    // ── Print individual student ─────────────────────────────────────────────

    private fun printStudentSummary(student: Student) {
        lifecycleScope.launch {
            try {
                val uid = student.uid.ifEmpty { student.id }
                val payments = firebaseRepo.getPaymentsByUid(uid).ifEmpty {
                    firebaseRepo.getPaymentsByStudentId(student.registrationNumber)
                }
                val hostelList = firebaseRepo.getHostelRequestsByUid(uid).ifEmpty {
                    firebaseRepo.getHostelRequestsByStudentId(student.registrationNumber)
                }
                val libraryList = firebaseRepo.getLibraryRequestsByUid(uid).ifEmpty {
                    firebaseRepo.getLibraryRequestsByStudentId(student.registrationNumber)
                }
                val html = pdfGenerator.generateComprehensiveStudentReport(
                    student, hostelList.firstOrNull(), libraryList, payments
                )
                pdfGenerator.printHtmlContent(html, "Student_${student.registrationNumber}_Report")
            } catch (e: Exception) {
                Log.e("StudentsActivity", "Print error", e)
                showError("Print failed: ${e.message}")
            }
        }
    }

    // ── Print all ────────────────────────────────────────────────────────────

    private fun printAllStudents() {
        val toPrint = if (currentFilter == "CONFIRMED")
            allStudents.filter { it.registrationStatus == "CONFIRMED" }
        else allStudents

        if (toPrint.isEmpty()) {
            AlertDialog.Builder(this).setTitle("No Students").setMessage("No students to print.").setPositiveButton("OK", null).show()
            return
        }

        lifecycleScope.launch {
            try {
                val html = buildAllStudentsHtml(toPrint)
                pdfGenerator.printHtmlContent(html, "All_Students_Report")
            } catch (e: Exception) {
                showError("Failed to generate report: ${e.message}")
            }
        }
    }

    private suspend fun buildAllStudentsHtml(students: List<Student>): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val now = sdf.format(Date())
        val sb = StringBuilder()

        sb.append("""<!DOCTYPE html><html><head><meta charset="UTF-8">
<style>
  body{font-family:Arial,sans-serif;margin:20px;color:#333;font-size:11pt;}
  h1{color:#156504;text-align:center;font-size:18pt;margin-bottom:4px;}
  h2{color:#156504;text-align:center;font-size:13pt;margin-top:0;}
  .meta{text-align:center;color:#555;margin-bottom:20px;font-size:10pt;}
  .sb{border:2px solid #156504;border-radius:6px;padding:14px;margin-bottom:24px;page-break-inside:avoid;}
  .sh{background:#156504;color:white;padding:5px 10px;margin:10px -14px;font-weight:bold;font-size:10pt;}
  .fr{display:flex;padding:3px 0;border-bottom:1px solid #eee;}
  .fl{font-weight:bold;width:190px;color:#444;font-size:10pt;}
  .fv{flex:1;font-size:10pt;}
  table{width:100%;border-collapse:collapse;margin-top:6px;font-size:9pt;}
  th{background:#156504;color:white;padding:5px;text-align:left;}
  td{border:1px solid #ddd;padding:4px 6px;}
  tr:nth-child(even){background:#f9f9f9;}
  .ok{color:#2E7D32;font-weight:bold;}.bad{color:#D32F2F;font-weight:bold;}
  .nd{color:#888;font-style:italic;font-size:10pt;padding:4px 0;}
  .footer{text-align:center;margin-top:30px;font-size:9pt;color:#888;border-top:1px solid #ddd;padding-top:10px;}
</style></head><body>
<h1>DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY</h1>
<h2>Student Registration Report — All Students</h2>
<div class="meta">Generated: $now &nbsp;|&nbsp; Total Students: ${students.size}</div>""")

        for ((idx, s) in students.withIndex()) {
            val uid = s.uid.ifEmpty { s.id }
            val payments = try { firebaseRepo.getPaymentsByUid(uid).ifEmpty { firebaseRepo.getPaymentsByStudentId(s.registrationNumber) } } catch (_: Exception) { emptyList() }
            val hostel = try { (firebaseRepo.getHostelRequestsByUid(uid).ifEmpty { firebaseRepo.getHostelRequestsByStudentId(s.registrationNumber) }).firstOrNull() } catch (_: Exception) { null }
            val libs = try { firebaseRepo.getLibraryRequestsByUid(uid).ifEmpty { firebaseRepo.getLibraryRequestsByStudentId(s.registrationNumber) } } catch (_: Exception) { emptyList() }
            val paid = payments.filter { it.paymentStatus == "SUCCESS" }.sumOf { it.amount }

            fun row(l: String, v: String) = "<div class='fr'><div class='fl'>$l</div><div class='fv'>$v</div></div>"

            sb.append("<div class='sb'><strong style='color:#156504;font-size:12pt;'>#${idx+1}. ${s.fullName}</strong>")
            sb.append("<span style='float:right;font-size:10pt;color:#888;'>${s.registrationNumber}</span>")
            sb.append("<div class='sh'>Personal &amp; Academic Information</div>")
            sb.append(row("Full Names", s.fullName))
            sb.append(row("National ID Number", s.idNumber))
            sb.append(row("Registration Number", s.registrationNumber))
            sb.append(row("Gender", s.gender))
            sb.append(row("Course", s.course))
            sb.append(row("Department", s.department))
            sb.append(row("Reg. Status", "<span class='${if(s.registrationStatus=="CONFIRMED") "ok" else ""}'>${s.registrationStatus}</span>"))
            sb.append(row("Payment Status", "<span class='${if(s.paymentStatus=="PAID") "ok" else "bad"}'>${s.paymentStatus}</span>"))
            sb.append(row("Date Registered", dateFmt.format(Date(s.dateRegistered))))

            sb.append("<div class='sh'>Hostel Information</div>")
            if (hostel != null) {
                sb.append(row("Location", hostel.hostelLocation))
                sb.append(row("Room Type", hostel.roomType))
                sb.append(row("Sharable", hostel.sharable))
                sb.append(row("Request Date", dateFmt.format(Date(hostel.requestDate))))
            } else sb.append("<div class='nd'>No hostel request.</div>")

            sb.append("<div class='sh'>Library Book Requests (${libs.size})</div>")
            if (libs.isNotEmpty()) {
                sb.append("<table><thead><tr><th>ISBN</th><th>Title</th><th>Author</th><th>Borrowed</th><th>Return</th></tr></thead><tbody>")
                for (lib in libs) {
                    sb.append("<tr><td>${lib.bookIsbn}</td><td>${lib.bookTitle}</td><td>${lib.author}</td>")
                    sb.append("<td>${dateFmt.format(Date(lib.dateBorrowed))}</td><td>${dateFmt.format(Date(lib.dateOfReturn))}</td></tr>")
                }
                sb.append("</tbody></table>")
            } else sb.append("<div class='nd'>No library book requests.</div>")

            sb.append("<div class='sh'>Fees &amp; Payment Summary</div>")
            if (s.totalFeesPayable > 0) {
                sb.append(row("Total Fees Payable", "KES ${String.format("%,.2f", s.totalFeesPayable)}"))
                sb.append(row("Total Paid (Verified)", "KES ${String.format("%,.2f", paid)}"))
                sb.append(row("Outstanding Balance", "KES ${String.format("%,.2f", s.totalFeesPayable - paid)}"))
            } else {
                sb.append("<div class='nd'>Fee balance unavailable (not configured).</div>")
                if (paid > 0) sb.append(row("Total Verified Payments", "KES ${String.format("%,.2f", paid)}"))
            }
            if (payments.isNotEmpty()) {
                sb.append("<table><thead><tr><th>Date</th><th>Amount (KES)</th><th>Transaction Ref</th><th>Status</th></tr></thead><tbody>")
                for (p in payments) {
                    val cls = if (p.paymentStatus=="SUCCESS") "ok" else if (p.paymentStatus=="FAILED") "bad" else ""
                    sb.append("<tr><td>${dateFmt.format(Date(p.transactionDate))}</td><td>${String.format("%,.2f", p.amount)}</td>")
                    sb.append("<td>${p.transactionReference}</td><td><span class='$cls'>${p.paymentStatus}</span></td></tr>")
                }
                sb.append("</tbody></table>")
            }
            sb.append("</div>")
        }

        sb.append("<div class='footer'>DeKUT Student Registration System — Confidential — Generated $now<br/>&copy; ${Calendar.getInstance().get(Calendar.YEAR)} Dedan Kimathi University of Technology</div>")
        sb.append("</body></html>")
        return sb.toString()
    }

    private fun showError(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.status_error)).show()
    }
}
