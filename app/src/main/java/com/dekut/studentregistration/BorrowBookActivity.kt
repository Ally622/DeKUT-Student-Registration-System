package com.dekut.studentregistration

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.models.Book
import com.dekut.studentregistration.models.LibraryRequest
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.repository.OfflineRepository
import com.dekut.studentregistration.utils.NetworkUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BorrowBookActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOOK = "extra_book"
    }

    private lateinit var tvBookTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvIsbn: TextView
    private lateinit var tvDepartment: TextView

    private lateinit var etStudentRegNumber: TextInputEditText
    private lateinit var btnDateBorrowed: MaterialButton
    private lateinit var btnDateReturn: MaterialButton
    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRepo: FirebaseRepository
    private lateinit var offlineRepo: OfflineRepository

    private var selectedBook: Book? = null
    private var dateBorrowed: Long = 0L
    private var dateReturn: Long = 0L
    private var currentStudentUid: String = ""

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borrow_book)

        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()
        offlineRepo = OfflineRepository(this)

        currentStudentUid = auth.currentUser?.uid ?: ""

        @Suppress("DEPRECATION")
        selectedBook = intent.getParcelableExtra(EXTRA_BOOK)

        if (selectedBook == null) {
            Toast.makeText(this, "No book selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        populateBookDetails()
        preloadStudentInfo()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvBookTitle = findViewById(R.id.tvBookTitle)
        tvAuthor = findViewById(R.id.tvAuthor)
        tvIsbn = findViewById(R.id.tvIsbn)
        tvDepartment = findViewById(R.id.tvDepartment)

        etStudentRegNumber = findViewById(R.id.etStudentRegNumber)
        btnDateBorrowed = findViewById(R.id.btnDateBorrowed)
        btnDateReturn = findViewById(R.id.btnDateReturn)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun populateBookDetails() {
        val book = selectedBook ?: return
        tvBookTitle.text = book.title
        tvAuthor.text = "Author: ${book.author}"
        tvIsbn.text = "ISBN: ${book.isbn}"
        tvDepartment.text = "Department: ${book.department}"
    }

    private fun preloadStudentInfo() {
        if (currentStudentUid.isEmpty()) return
        lifecycleScope.launch {
            try {
                val student = firebaseRepo.getStudentByUid(currentStudentUid)
                if (student != null && etStudentRegNumber.text.isNullOrEmpty()) {
                    etStudentRegNumber.setText(student.registrationNumber)
                }
            } catch (e: Exception) {
                Log.e("BorrowBookActivity", "Preload student error", e)
            }
        }
    }

    private fun setupClickListeners() {
        btnDateBorrowed.setOnClickListener { showDatePicker(true) }
        btnDateReturn.setOnClickListener { showDatePicker(false) }
        btnSubmit.setOnClickListener { if (validateInput()) submitBorrowRequest() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun showDatePicker(isBorrowDate: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val selectedDate = calendar.timeInMillis
            if (isBorrowDate) {
                dateBorrowed = selectedDate
                btnDateBorrowed.text = "Borrow Date: ${dateFormat.format(Date(selectedDate))}"
            } else {
                dateReturn = selectedDate
                btnDateReturn.text = "Return Date: ${dateFormat.format(Date(selectedDate))}"
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validateInput(): Boolean {
        val book = selectedBook ?: return false
        if (book.availableCopies <= 0) {
            showError("This book '${book.title}' is currently out of stock.")
            return false
        }

        val regNumber = etStudentRegNumber.text.toString().trim()
        when {
            regNumber.isEmpty() -> {
                etStudentRegNumber.error = "Student Registration Number is required"
                etStudentRegNumber.requestFocus()
                return false
            }
            dateBorrowed == 0L -> {
                Toast.makeText(this, "Please select borrow date", Toast.LENGTH_SHORT).show()
                return false
            }
            dateReturn == 0L -> {
                Toast.makeText(this, "Please select expected return date", Toast.LENGTH_SHORT).show()
                return false
            }
            dateReturn <= dateBorrowed -> {
                Toast.makeText(this, "Expected return date must be after borrow date", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun submitBorrowRequest() {
        val book = selectedBook ?: return
        val regNumber = etStudentRegNumber.text.toString().trim().uppercase()

        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val existingStudent = firebaseRepo.getStudentByRegistrationNumber(regNumber)
                if (existingStudent == null && offlineRepo.getStudentByRegistrationNumber(regNumber) == null) {
                    showError("Invalid Registration Number: $regNumber does not match any registered student.")
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Borrow Request"
                    progressBar.visibility = View.GONE
                    return@launch
                }

                val request = LibraryRequest(
                    uid = currentStudentUid,
                    studentId = regNumber,
                    bookId = book.id,
                    bookIsbn = book.isbn,
                    bookTitle = book.title,
                    author = book.author,
                    department = book.department,
                    dateBorrowed = dateBorrowed,
                    dateOfReturn = dateReturn,
                    status = "PENDING"
                )

                val isOnline = NetworkUtils.isNetworkAvailable(this@BorrowBookActivity)
                if (isOnline) {
                    val result = firebaseRepo.saveLibraryRequest(request)
                    if (result.isSuccess) {
                        Toast.makeText(this@BorrowBookActivity, "✅ Borrowing request submitted successfully!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val err = result.exceptionOrNull()?.message ?: "Unknown error"
                        showError("Failed to submit request: $err")
                    }
                } else {
                    offlineRepo.saveLibraryRequestOffline(request)
                    Toast.makeText(this@BorrowBookActivity, "📱 Offline draft saved. Will sync when online.", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("BorrowBookActivity", "Submit borrow request error", e)
                showError("Error submitting request: ${e.message}")
            } finally {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Borrow Request"
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.status_error)).show()
}
