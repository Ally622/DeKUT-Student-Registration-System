package com.dekut.studentregistration.repository

import android.util.Log
import com.dekut.studentregistration.models.*
import com.dekut.studentregistration.utils.LibrarySeeder
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

class FirebaseRepository private constructor() {

    companion object {
        private const val TAG = "FirebaseRepository"
        private const val DATABASE_URL =
            "https://studentregistration-a018e-default-rtdb.firebaseio.com/"

        @Volatile
        private var instance: FirebaseRepository? = null
        fun getInstance(): FirebaseRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebaseRepository().also { instance = it }
            }
        }
    }

    private val database: FirebaseDatabase =
        FirebaseDatabase.getInstance(DATABASE_URL).apply {
            try {
                setPersistenceEnabled(true)
                Log.d(TAG, "Firebase persistence enabled")
            } catch (e: Exception) {
                Log.e(TAG, "Firebase persistence already enabled or failed", e)
            }
        }

    // Database paths — students are keyed by UID
    private val studentsRef: DatabaseReference = database.getReference("DeKUTRegistration/Students")
    private val libraryRef: DatabaseReference = database.getReference("DeKUTRegistration/LibraryRequests")
    private val hostelRef: DatabaseReference = database.getReference("DeKUTRegistration/HostelRequests")
    private val paymentsRef: DatabaseReference = database.getReference("DeKUTRegistration/Payments")
    private val booksRef: DatabaseReference = database.getReference("DeKUTRegistration/Books")

    init {
        Log.d(TAG, "Firebase initialized: $DATABASE_URL")
    }

    // ─── STUDENT OPERATIONS ──────────────────────────────────────────────────

    /** Save student using their Firebase UID as the database key. */
    suspend fun saveStudentWithUid(student: Student): Result<String> {
        return try {
            require(student.uid.isNotEmpty()) { "Student UID must not be empty" }
            student.id = student.uid
            student.syncStatus = "PENDING"
            Log.d(TAG, "Saving student under UID: ${student.uid}")
            withTimeout(15000) {
                studentsRef.child(student.uid).setValue(student).await()
            }
            student.syncStatus = "SYNCED"
            Log.d(TAG, "✅ Student saved: ${student.uid}")
            Result.success(student.uid)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save student with UID", e)
            Result.failure(e)
        }
    }

    /** Legacy save — generates a push key if id is empty. */
    suspend fun saveStudent(student: Student): Result<String> {
        return try {
            val key = if (student.id.isEmpty()) studentsRef.push().key
                ?: throw Exception("Failed to generate student ID") else student.id
            student.id = key
            Log.d(TAG, "Saving student: ${student.registrationNumber}")
            withTimeout(15000) {
                studentsRef.child(key).setValue(student).await()
            }
            student.syncStatus = "SYNCED"
            Log.d(TAG, "✅ Student saved: $key")
            Result.success(key)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save student", e)
            Result.failure(e)
        }
    }

    /** Retrieve a student by their Firebase UID (their database key). */
    suspend fun getStudentByUid(uid: String): Student? {
        return try {
            withTimeoutOrNull(5000) {
                val snap = studentsRef.child(uid).get().await()
                snap.getValue(Student::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get student by UID: $uid", e)
            null
        }
    }

    suspend fun getStudentByRegistrationNumber(regNumber: String): Student? {
        return try {
            val normalizedRegNumber = regNumber.trim().uppercase()
            withTimeoutOrNull(5000) {
                val snapshot = studentsRef
                    .orderByChild("registrationNumber").equalTo(normalizedRegNumber)
                    .limitToFirst(1).get().await()
                snapshot.children.firstOrNull()?.getValue(Student::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve student by regNumber", e)
            null
        }
    }

    /** Check if National ID number already exists. */
    suspend fun getStudentByIdNumber(idNumber: String): Student? {
        return try {
            val normalizedIdNumber = idNumber.trim()
            if (normalizedIdNumber.isEmpty()) return null
            withTimeoutOrNull(5000) {
                val snapshot = studentsRef
                    .orderByChild("idNumber").equalTo(normalizedIdNumber)
                    .limitToFirst(1).get().await()
                snapshot.children.firstOrNull()?.getValue(Student::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve student by idNumber", e)
            null
        }
    }

    suspend fun getStudentById(studentId: String): Student? {
        return try {
            withTimeoutOrNull(5000) {
                studentsRef.child(studentId).get().await().getValue(Student::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get student by id", e)
            null
        }
    }

    fun getAllStudents(): Flow<List<Student>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val students = snapshot.children.mapNotNull { it.getValue(Student::class.java) }
                trySend(students)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getAllStudents cancelled: ${error.message}, code: ${error.code}")
                // Handle permission denied gracefully - send empty list instead of crashing
                when (error.code) {
                    DatabaseError.PERMISSION_DENIED -> {
                        Log.w(TAG, "Permission denied accessing all students. User may not be admin.")
                        trySend(emptyList()) // Send empty list instead of crashing
                        close() // Close without exception
                    }
                    else -> {
                        Log.e(TAG, "Database error: ${error.details}")
                        trySend(emptyList()) // Send empty list for any error
                        close() // Close without throwing exception
                    }
                }
            }
        }
        studentsRef.addValueEventListener(listener)
        awaitClose { studentsRef.removeEventListener(listener) }
    }

    suspend fun updateStudentPaymentStatus(studentId: String, isPaid: Boolean): Result<Unit> {
        return try {
            studentsRef.child(studentId).child("paymentStatus")
                .setValue(if (isPaid) "PAID" else "UNPAID").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update payment status", e)
            Result.failure(e)
        }
    }

    suspend fun updateStudentRegistrationStatus(studentId: String, status: String): Result<Unit> {
        return try {
            studentsRef.child(studentId).child("registrationStatus").setValue(status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update registration status", e)
            Result.failure(e)
        }
    }

    // ─── LIBRARY OPERATIONS ──────────────────────────────────────────────────

    suspend fun saveLibraryRequest(request: LibraryRequest): Result<String> {
        return try {
            val key = if (request.id.isEmpty()) libraryRef.push().key
                ?: throw Exception("Failed to generate library request ID") else request.id
            request.id = key
            libraryRef.child(key).setValue(request).await()
            request.syncStatus = "SYNCED"
            Log.d(TAG, "Library request saved: $key")
            Result.success(key)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save library request", e)
            Result.failure(e)
        }
    }

    /** Get library requests by studentId field (registration number). */
    suspend fun getLibraryRequestsByStudentId(studentId: String): List<LibraryRequest> {
        return try {
            val snap = libraryRef.orderByChild("studentId").equalTo(studentId).get().await()
            snap.children.mapNotNull { it.getValue(LibraryRequest::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve library requests for $studentId", e)
            emptyList()
        }
    }

    /** Get library requests by uid field. */
    suspend fun getLibraryRequestsByUid(uid: String): List<LibraryRequest> {
        return try {
            val snap = libraryRef.orderByChild("uid").equalTo(uid).get().await()
            snap.children.mapNotNull { it.getValue(LibraryRequest::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve library requests for uid $uid", e)
            emptyList()
        }
    }

    // ─── BOOK CATALOGUE & ADVANCED LIBRARY OPERATIONS ──────────────────────────

    suspend fun saveBook(book: Book): Result<String> {
        return try {
            val key = if (book.id.isEmpty()) {
                if (book.isbn.isNotEmpty()) book.isbn.replace("-", "").replace(" ", "")
                else booksRef.push().key ?: throw Exception("Failed to generate book ID")
            } else book.id
            book.id = key
            if (book.availableCopies > book.totalCopies) book.availableCopies = book.totalCopies
            book.availabilityStatus = if (book.availableCopies > 0) "AVAILABLE" else "UNAVAILABLE"
            withTimeout(15000) {
                booksRef.child(key).setValue(book).await()
            }
            Log.d(TAG, "Book saved: $key")
            Result.success(key)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save book", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBook(bookId: String): Result<Unit> {
        return try {
            withTimeout(15000) {
                booksRef.child(bookId).removeValue().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete book: $bookId", e)
            Result.failure(e)
        }
    }

    suspend fun getBookById(bookId: String): Book? {
        return try {
            withTimeoutOrNull(5000) {
                booksRef.child(bookId).get().await().getValue(Book::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get book by ID: $bookId", e)
            null
        }
    }

    fun getAllBooks(): Flow<List<Book>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = snapshot.children.mapNotNull { it.getValue(Book::class.java) }
                trySend(books)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getAllBooks cancelled: ${error.message}")
                trySend(emptyList())
                close()
            }
        }
        booksRef.addValueEventListener(listener)
        awaitClose { booksRef.removeEventListener(listener) }
    }

    suspend fun seedSampleBooks(): Result<Int> {
        return try {
            val sampleBooks = LibrarySeeder.getSampleBooks()
            var count = 0
            for (book in sampleBooks) {
                saveBook(book)
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seed sample books", e)
            Result.failure(e)
        }
    }

    fun getAllLibraryRequests(): Flow<List<LibraryRequest>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children.mapNotNull { it.getValue(LibraryRequest::class.java) }
                trySend(requests)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getAllLibraryRequests cancelled: ${error.message}")
                trySend(emptyList())
                close()
            }
        }
        libraryRef.addValueEventListener(listener)
        awaitClose { libraryRef.removeEventListener(listener) }
    }

    suspend fun updateLibraryRequestStatus(requestId: String, newStatus: String, bookId: String): Result<Unit> {
        return try {
            libraryRef.child(requestId).child("status").setValue(newStatus).await()
            if (bookId.isNotEmpty()) {
                val book = getBookById(bookId)
                if (book != null) {
                    when (newStatus) {
                        "APPROVED" -> {
                            if (book.availableCopies > 0) {
                                book.availableCopies -= 1
                                book.availabilityStatus = if (book.availableCopies > 0) "AVAILABLE" else "UNAVAILABLE"
                                saveBook(book)
                            }
                        }
                        "RETURNED" -> {
                            if (book.availableCopies < book.totalCopies) {
                                book.availableCopies += 1
                                book.availabilityStatus = "AVAILABLE"
                                saveBook(book)
                            }
                        }
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update library request status", e)
            Result.failure(e)
        }
    }

    // ─── HOSTEL OPERATIONS ───────────────────────────────────────────────────

    suspend fun saveHostelRequest(request: HostelRequest): Result<String> {
        return try {
            val key = if (request.id.isEmpty()) hostelRef.push().key
                ?: throw Exception("Failed to generate hostel request ID") else request.id
            request.id = key
            hostelRef.child(key).setValue(request).await()
            request.syncStatus = "SYNCED"
            Log.d(TAG, "Hostel request saved: $key")
            Result.success(key)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save hostel request", e)
            Result.failure(e)
        }
    }

    suspend fun getHostelRequestsByStudentId(studentId: String): List<HostelRequest> {
        return try {
            val snap = hostelRef.orderByChild("studentId").equalTo(studentId).get().await()
            snap.children.mapNotNull { it.getValue(HostelRequest::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve hostel requests for $studentId", e)
            emptyList()
        }
    }

    suspend fun getHostelRequestsByUid(uid: String): List<HostelRequest> {
        return try {
            val snap = hostelRef.orderByChild("uid").equalTo(uid).get().await()
            snap.children.mapNotNull { it.getValue(HostelRequest::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve hostel requests for uid $uid", e)
            emptyList()
        }
    }

    // ─── PAYMENT OPERATIONS ──────────────────────────────────────────────────

    suspend fun savePayment(payment: Payment): Result<String> {
        return try {
            val key = if (payment.id.isEmpty()) paymentsRef.push().key
                ?: throw Exception("Failed to generate payment ID") else payment.id
            payment.id = key
            paymentsRef.child(key).setValue(payment).await()
            payment.syncStatus = "SYNCED"
            Log.d(TAG, "Payment saved: $key")
            Result.success(key)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save payment", e)
            Result.failure(e)
        }
    }

    suspend fun getPaymentsByStudentId(studentId: String): List<Payment> {
        return try {
            val snap = paymentsRef.orderByChild("studentId").equalTo(studentId).get().await()
            snap.children.mapNotNull { it.getValue(Payment::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve payments for $studentId", e)
            emptyList()
        }
    }

    suspend fun getPaymentsByUid(uid: String): List<Payment> {
        return try {
            val snap = paymentsRef.orderByChild("uid").equalTo(uid).get().await()
            snap.children.mapNotNull { it.getValue(Payment::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve payments for uid $uid", e)
            emptyList()
        }
    }

    suspend fun updatePaymentStatus(paymentId: String, status: String, transactionRef: String): Result<Unit> {
        return try {
            paymentsRef.child(paymentId).updateChildren(
                mapOf("paymentStatus" to status, "transactionReference" to transactionRef)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update payment status", e)
            Result.failure(e)
        }
    }
}
