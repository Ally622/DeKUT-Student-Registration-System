package com.dekut.studentregistration

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dekut.studentregistration.adapters.BooksAdapter
import com.dekut.studentregistration.models.Book
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.utils.LibrarySeeder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class LibraryActivity : AppCompatActivity() {

    private lateinit var spinnerDepartmentFilter: Spinner
    private lateinit var etSearchQuery: TextInputEditText
    private lateinit var rvBooks: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    private lateinit var firebaseRepo: FirebaseRepository
    private lateinit var booksAdapter: BooksAdapter
    private var allBooksList: List<Book> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        firebaseRepo = FirebaseRepository.getInstance()

        initializeViews()
        setupRecyclerView()
        setupDepartmentSpinner()
        setupSearchListener()
        loadBooksCatalogue()
    }

    private fun initializeViews() {
        spinnerDepartmentFilter = findViewById(R.id.spinnerDepartmentFilter)
        etSearchQuery = findViewById(R.id.etSearchQuery)
        rvBooks = findViewById(R.id.rvBooks)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
    }

    private fun setupRecyclerView() {
        booksAdapter = BooksAdapter(emptyList()) { book ->
            onBookSelected(book)
        }
        rvBooks.layoutManager = LinearLayoutManager(this)
        rvBooks.adapter = booksAdapter
    }

    private fun setupDepartmentSpinner() {
        val departmentsList = mutableListOf("All Departments")
        departmentsList.addAll(LibrarySeeder.DEPARTMENTS)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDepartmentFilter.adapter = adapter

        spinnerDepartmentFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterBooks()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearchListener() {
        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBooks()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadBooksCatalogue() {
        // Pre-populate with local 90-book catalogue so books are displayed immediately
        allBooksList = LibrarySeeder.getSampleBooks()
        filterBooks()

        lifecycleScope.launch {
            firebaseRepo.getAllBooks()
                .catch { e ->
                    Log.e("LibraryActivity", "Error loading books from Firebase", e)
                }
                .collect { books ->
                    if (books.size < 50) {
                        Log.d("LibraryActivity", "Firebase catalogue incomplete (${books.size} books), seeding full 90-book catalogue...")
                        firebaseRepo.seedSampleBooks()
                    } else {
                        allBooksList = books
                        filterBooks()
                    }
                }
        }
    }

    private fun filterBooks() {
        val selectedDept = spinnerDepartmentFilter.selectedItem?.toString() ?: "All Departments"
        val query = etSearchQuery.text.toString().trim().lowercase()

        val filtered = allBooksList.filter { book ->
            val matchesDept = selectedDept == "All Departments" ||
                    query.isNotEmpty() ||
                    book.department.equals(selectedDept, ignoreCase = true)

            val matchesQuery = query.isEmpty() ||
                    book.title.lowercase().contains(query) ||
                    book.isbn.lowercase().contains(query) ||
                    book.author.lowercase().contains(query) ||
                    book.department.lowercase().contains(query)

            matchesDept && matchesQuery
        }

        booksAdapter.updateBooks(filtered)
        if (filtered.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvBooks.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            rvBooks.visibility = View.VISIBLE
        }
    }

    private fun onBookSelected(book: Book) {
        val intent = Intent(this, BorrowBookActivity::class.java).apply {
            putExtra(BorrowBookActivity.EXTRA_BOOK, book)
        }
        startActivity(intent)
    }
}
