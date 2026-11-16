package com.dekut.studentregistration

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log

import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dekut.studentregistration.adapters.AdminLibraryRequestsAdapter
import com.dekut.studentregistration.adapters.BooksAdapter
import com.dekut.studentregistration.models.Book
import com.dekut.studentregistration.models.LibraryRequest
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.utils.LibrarySeeder
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AdminLibraryActivity : AppCompatActivity() {

    private lateinit var btnAddBook: MaterialButton
    private lateinit var btnImportCsv: MaterialButton
    private lateinit var etAdminSearch: TextInputEditText
    private lateinit var rvAdminBooks: RecyclerView
    private lateinit var rvAdminRequests: RecyclerView

    private lateinit var firebaseRepo: FirebaseRepository

    private lateinit var booksAdapter: BooksAdapter
    private lateinit var requestsAdapter: AdminLibraryRequestsAdapter

    private var allBooks: List<Book> = emptyList()
    private var allRequests: List<LibraryRequest> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_library)

        firebaseRepo = FirebaseRepository.getInstance()

        initializeViews()
        setupRecyclerViews()
        setupSearchListener()
        setupClickListeners()
        loadData()
    }

    private fun initializeViews() {
        btnAddBook = findViewById(R.id.btnAddBook)
        btnImportCsv = findViewById(R.id.btnImportCsv)
        etAdminSearch = findViewById(R.id.etAdminSearch)
        rvAdminBooks = findViewById(R.id.rvAdminBooks)
        rvAdminRequests = findViewById(R.id.rvAdminRequests)
    }

    private fun setupRecyclerViews() {
        booksAdapter = BooksAdapter(emptyList()) { book ->
            showEditDeleteBookDialog(book)
        }
        rvAdminBooks.layoutManager = LinearLayoutManager(this)
        rvAdminBooks.adapter = booksAdapter

        requestsAdapter = AdminLibraryRequestsAdapter(
            emptyList(),
            onApprove = { req -> updateRequestStatus(req, "APPROVED") },
            onReject = { req -> updateRequestStatus(req, "REJECTED") },
            onReturn = { req -> updateRequestStatus(req, "RETURNED") }
        )
        rvAdminRequests.layoutManager = LinearLayoutManager(this)
        rvAdminRequests.adapter = requestsAdapter
    }

    private fun setupSearchListener() {
        etAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClickListeners() {
        btnAddBook.setOnClickListener { showAddBookDialog(null) }
        btnImportCsv.setOnClickListener { showImportCsvDialog() }
    }

    private fun loadData() {
        lifecycleScope.launch {
            firebaseRepo.getAllBooks()
                .catch { e -> Log.e("AdminLibrary", "Error loading books", e) }
                .collect { books ->
                    allBooks = books
                    filterData()
                }
        }

        lifecycleScope.launch {
            firebaseRepo.getAllLibraryRequests()
                .catch { e -> Log.e("AdminLibrary", "Error loading requests", e) }
                .collect { requests ->
                    allRequests = requests
                    filterData()
                }
        }
    }

    private fun filterData() {
        val query = etAdminSearch.text.toString().trim().lowercase()

        val filteredBooks = if (query.isEmpty()) allBooks else {
            allBooks.filter {
                it.title.lowercase().contains(query) ||
                        it.isbn.lowercase().contains(query) ||
                        it.author.lowercase().contains(query) ||
                        it.department.lowercase().contains(query)
            }
        }
        booksAdapter.updateBooks(filteredBooks)

        val filteredRequests = if (query.isEmpty()) allRequests else {
            allRequests.filter {
                it.bookTitle.lowercase().contains(query) ||
                        it.studentId.lowercase().contains(query) ||
                        it.bookIsbn.lowercase().contains(query) ||
                        it.status.lowercase().contains(query)
            }
        }
        requestsAdapter.updateRequests(filteredRequests)
    }

    private fun showAddBookDialog(existingBook: Book?) {
        val builder = AlertDialog.Builder(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 10)
        }

        val etTitle = EditText(this).apply { hint = "Book Title"; setText(existingBook?.title ?: "") }
        val etIsbn = EditText(this).apply { hint = "ISBN"; setText(existingBook?.isbn ?: "") }
        val etAuthor = EditText(this).apply { hint = "Author"; setText(existingBook?.author ?: "") }
        val etCopies = EditText(this).apply {
            hint = "Total Copies"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(existingBook?.totalCopies?.toString() ?: "5")
        }

        val spinnerDept = Spinner(this)
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, LibrarySeeder.DEPARTMENTS)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDept.adapter = deptAdapter
        existingBook?.let { b ->
            val idx = LibrarySeeder.DEPARTMENTS.indexOf(b.department)
            if (idx >= 0) spinnerDept.setSelection(idx)
        }

        container.addView(TextView(this).apply { text = if (existingBook == null) "Add New Book" else "Edit Book"; textSize = 18f; setTypeface(null, Typeface.BOLD) })
        container.addView(etTitle)
        container.addView(etIsbn)
        container.addView(etAuthor)
        container.addView(TextView(this).apply { text = "Select Department:"; setPadding(0, 20, 0, 5) })
        container.addView(spinnerDept)
        container.addView(etCopies)

        builder.setView(container)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val isbn = etIsbn.text.toString().trim()
                val author = etAuthor.text.toString().trim()
                val dept = spinnerDept.selectedItem.toString()
                val copies = etCopies.text.toString().toIntOrNull() ?: 1

                if (title.isEmpty() || isbn.isEmpty()) {
                    showError("Title and ISBN are required.")
                    return@setPositiveButton
                }

                val book = existingBook?.copy(
                    title = title,
                    isbn = isbn,
                    author = author,
                    department = dept,
                    totalCopies = copies,
                    availableCopies = if (existingBook.availableCopies > copies) copies else copies,
                    availabilityStatus = if (copies > 0) "AVAILABLE" else "UNAVAILABLE"
                ) ?: Book(
                    id = isbn.replace("-", "").replace(" ", ""),
                    title = title,
                    isbn = isbn,
                    author = author,
                    department = dept,
                    totalCopies = copies,
                    availableCopies = copies,
                    availabilityStatus = "AVAILABLE"
                )

                lifecycleScope.launch {
                    val result = firebaseRepo.saveBook(book)
                    if (result.isSuccess) {
                        showSuccess("✅ Book '${book.title}' saved successfully!")
                    } else {
                        showError("Failed to save book: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDeleteBookDialog(book: Book) {
        val options = arrayOf("Edit Book Details", "Delete Book", "Cancel")
        AlertDialog.Builder(this)
            .setTitle(book.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddBookDialog(book)
                    1 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Delete Book")
                            .setMessage("Are you sure you want to delete '${book.title}'?")
                            .setPositiveButton("Delete") { _, _ ->
                                lifecycleScope.launch {
                                    val res = firebaseRepo.deleteBook(book.id)
                                    if (res.isSuccess) showSuccess("Book deleted.")
                                    else showError("Failed to delete book.")
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }
            .show()
    }

    private fun showImportCsvDialog() {
        val options = arrayOf("Import Sample Books Catalogue", "Paste CSV Content", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Bulk Import Books")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        lifecycleScope.launch {
                            val result = firebaseRepo.seedSampleBooks()
                            if (result.isSuccess) {
                                showSuccess("✅ Sample books imported across all 9 departments!")
                            } else showError("Import failed.")
                        }
                    }
                    1 -> showPasteCsvDialog()
                }
            }
            .show()
    }

    private fun showPasteCsvDialog() {
        val etCsv = EditText(this).apply {
            hint = "Format: department,title,ISBN,author,total_copies\n\nExample:\nSchool of Engineering,Thermo,978-123,Author,5"
            minLines = 5
        }
        AlertDialog.Builder(this)
            .setTitle("Import CSV")
            .setView(etCsv)
            .setPositiveButton("Import") { _, _ ->
                val csv = etCsv.text.toString().trim()
                if (csv.isEmpty()) return@setPositiveButton

                val (books, rejected) = LibrarySeeder.parseCsvContent(csv)
                lifecycleScope.launch {
                    var successCount = 0
                    for (b in books) {
                        if (firebaseRepo.saveBook(b).isSuccess) successCount++
                    }
                    showSuccess("Import complete: $successCount books added, $rejected rejected.")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateRequestStatus(request: LibraryRequest, newStatus: String) {
        lifecycleScope.launch {
            val result = firebaseRepo.updateLibraryRequestStatus(request.id, newStatus, request.bookId)
            if (result.isSuccess) {
                showSuccess("Request status updated to $newStatus")
            } else {
                showError("Failed to update status: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun showSuccess(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.status_success)).show()
    private fun showError(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.status_error)).show()
}
