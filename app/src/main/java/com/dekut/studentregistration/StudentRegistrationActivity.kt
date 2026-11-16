package com.dekut.studentregistration

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.models.Student
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.repository.OfflineRepository
import com.dekut.studentregistration.utils.NetworkUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class StudentRegistrationActivity : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText
    private lateinit var etIdNumber: TextInputEditText
    private lateinit var etRegNumber: TextInputEditText
    private lateinit var rgGender: RadioGroup
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var spinnerCourse: Spinner
    private lateinit var spinnerDepartment: Spinner
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnClear: MaterialButton
    private lateinit var tvSyncStatus: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRepo: FirebaseRepository
    private lateinit var offlineRepo: OfflineRepository

    private var isProcessingRegistration = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_registration)

        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()
        offlineRepo = OfflineRepository(this)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.etFullName)
        etIdNumber = findViewById(R.id.etIdNumber)
        etRegNumber = findViewById(R.id.etRegNumber)
        rgGender = findViewById(R.id.rgGender)
        rbMale = findViewById(R.id.rbMale)
        rbFemale = findViewById(R.id.rbFemale)
        spinnerCourse = findViewById(R.id.spinnerCourse)
        spinnerDepartment = findViewById(R.id.spinnerDepartment)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnClear = findViewById(R.id.btnClear)
        tvSyncStatus = findViewById(R.id.tvSyncStatus)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            if (!isProcessingRegistration && validateInput()) {
                registerStudent()
            }
        }
        btnClear.setOnClickListener { clearForm() }
    }

    private fun validateInput(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val idNumber = etIdNumber.text.toString().trim()
        val regNumber = etRegNumber.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        when {
            fullName.isEmpty() -> {
                etFullName.error = getString(R.string.field_required)
                etFullName.requestFocus(); return false
            }
            fullName.length < 3 -> {
                etFullName.error = "Full name too short"
                etFullName.requestFocus(); return false
            }
            idNumber.isEmpty() || idNumber.length != 9 -> {
                etIdNumber.error = "ID number must be exactly 9 digits"
                etIdNumber.requestFocus(); return false
            }
            !idNumber.all { it.isDigit() } -> {
                etIdNumber.error = "ID number must contain digits only"
                etIdNumber.requestFocus(); return false
            }
            regNumber.isEmpty() -> {
                etRegNumber.error = getString(R.string.field_required)
                etRegNumber.requestFocus(); return false
            }
            rgGender.checkedRadioButtonId == -1 -> {
                Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
                return false
            }
            spinnerCourse.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show()
                return false
            }
            spinnerDepartment.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show()
                return false
            }
            password.isEmpty() -> {
                etPassword.error = "Password is required"
                etPassword.requestFocus(); return false
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus(); return false
            }
            confirmPassword.isEmpty() -> {
                etConfirmPassword.error = "Please confirm your password"
                etConfirmPassword.requestFocus(); return false
            }
            password != confirmPassword -> {
                etConfirmPassword.error = "Passwords do not match"
                etConfirmPassword.requestFocus(); return false
            }
        }
        return true
    }

    private fun registerStudent() {
        if (isProcessingRegistration) return
        isProcessingRegistration = true

        val fullName = etFullName.text.toString().trim()
        val idNumber = etIdNumber.text.toString().trim()
        val regNumber = etRegNumber.text.toString().trim().uppercase()
        val gender = if (rbMale.isChecked) "Male" else "Female"
        val course = spinnerCourse.selectedItem.toString()
        val department = spinnerDepartment.selectedItem.toString()
        val password = etPassword.text.toString()

        btnRegister.isEnabled = false
        btnRegister.text = "Registering..."
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            var navigateOnSuccess = false
            try {
                val isOnline = NetworkUtils.isNetworkAvailable(this@StudentRegistrationActivity)
                Log.d("StudentReg", "Network available: $isOnline, regNumber: $regNumber")

                if (!isOnline) {
                    // Save draft offline — Auth account creation needs internet
                    val draft = Student(
                        fullName = fullName, idNumber = idNumber,
                        registrationNumber = regNumber, gender = gender,
                        course = course, department = department,
                        registrationStatus = "DRAFT", paymentStatus = "UNPAID",
                        syncStatus = "PENDING"
                    )
                    offlineRepo.saveStudentOffline(draft)
                    tvSyncStatus.visibility = View.VISIBLE
                    tvSyncStatus.text = "⏳ Draft saved. Account will be created when you are online."
                    tvSyncStatus.setBackgroundColor(getColor(R.color.status_pending))
                    showSuccess("Saved as draft. Come back online to complete registration.")
                    resetForm()
                    return@launch
                }

                // --- ONLINE PATH ---
                val email = "${regNumber.lowercase().replace("/", ".").replace(" ", "")}@dekut.student"
                Log.d("StudentReg", "Processing registration for email: $email")

                // Step 1: Obtain or Recover Firebase Authentication user
                var currentUser = auth.currentUser
                if (currentUser != null && currentUser.email?.lowercase() != email) {
                    auth.signOut()
                    currentUser = null
                }

                if (currentUser == null) {
                    try {
                        Log.d("StudentReg", "Creating Firebase Auth account...")
                        val authResult = withTimeout(15000) {
                            auth.createUserWithEmailAndPassword(email, password).await()
                        }
                        currentUser = authResult.user
                        Log.d("StudentReg", "Auth account created successfully, UID: ${currentUser?.uid}")
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Log.d("StudentReg", "Auth collision for $email: ${e.message}. Attempting sign-in recovery...")
                        try {
                            val signInResult = withTimeout(15000) {
                                auth.signInWithEmailAndPassword(email, password).await()
                            }
                            currentUser = signInResult.user
                            Log.d("StudentReg", "Signed in to existing Auth account for recovery, UID: ${currentUser?.uid}")
                        } catch (signInEx: Exception) {
                            Log.w("StudentReg", "Sign-in during collision failed", signInEx)
                            showError("Registration number $regNumber is already registered. If this is your account, please log in.")
                            resetButtonOnly()
                            return@launch
                        }
                    }
                }

                val uid = currentUser?.uid
                    ?: throw Exception("Firebase Auth did not return a user UID.")

                // Step 2: Check if profile already exists in Realtime Database under this UID
                val existingProfile = firebaseRepo.getStudentByUid(uid)
                if (existingProfile != null) {
                    Log.w("StudentReg", "Student profile already exists in DB for UID: $uid")
                    showError("Registration number $regNumber is already registered. Please log in.")
                    auth.signOut()
                    resetButtonOnly()
                    return@launch
                }

                // Step 3: Build student record linked to UID
                val student = Student(
                    id = uid,
                    uid = uid,
                    fullName = fullName,
                    idNumber = idNumber,
                    registrationNumber = regNumber,
                    gender = gender,
                    course = course,
                    department = department,
                    registrationStatus = "PENDING",
                    paymentStatus = "UNPAID",
                    role = "STUDENT",
                    syncStatus = "SYNCED"
                )

                // Step 4: Save student profile to Firebase Realtime Database
                Log.d("StudentReg", "Saving student profile under UID: $uid")
                val saveResult = firebaseRepo.saveStudentWithUid(student)

                if (saveResult.isSuccess) {
                    Log.d("StudentReg", "✅ Student profile saved to Firebase")
                    tvSyncStatus.visibility = View.GONE
                    showSuccess("✅ Registration successful! Navigating to login...")
                    navigateOnSuccess = true

                    Handler(Looper.getMainLooper()).postDelayed({
                        auth.signOut()
                        val intent = Intent(this@StudentRegistrationActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }, 2000)

                } else {
                    val err = saveResult.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("StudentReg", "Save student profile failed: $err")
                    showError("Registration profile failed to save: $err\n\nPlease tap Register again to complete profile setup.")
                    resetButtonOnly()
                }

            } catch (e: TimeoutCancellationException) {
                Log.e("StudentReg", "Registration operation timed out", e)
                showError("⏳ Registration operation timed out. Please check your network connection and try again.")
                resetButtonOnly()
            } catch (e: FirebaseAuthWeakPasswordException) {
                Log.e("StudentReg", "Weak password", e)
                showError("Password is too weak. Please use at least 6 characters.")
                resetButtonOnly()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e("StudentReg", "Invalid credentials", e)
                showError("Invalid registration information or password format.")
                resetButtonOnly()
            } catch (e: Exception) {
                Log.e("StudentReg", "Registration error", e)
                val msg = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Check your internet connection."
                    e.message?.contains("permission", ignoreCase = true) == true ->
                        "Permission denied. Contact system administrator."
                    else -> e.message ?: "An unexpected error occurred."
                }
                showError("❌ $msg")
                resetButtonOnly()
            } finally {
                if (!navigateOnSuccess) {
                    isProcessingRegistration = false
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun resetButtonOnly() {
        isProcessingRegistration = false
        btnRegister.isEnabled = true
        btnRegister.text = getString(R.string.register)
        progressBar.visibility = View.GONE
    }

    private fun resetForm() {
        isProcessingRegistration = false
        btnRegister.isEnabled = true
        btnRegister.text = getString(R.string.register)
        progressBar.visibility = View.GONE
    }

    private fun showSuccess(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.status_success)).show()
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.status_error)).show()
    }

    private fun clearForm() {
        etFullName.text?.clear(); etIdNumber.text?.clear()
        etRegNumber.text?.clear(); rgGender.clearCheck()
        etPassword.text?.clear(); etConfirmPassword.text?.clear()
        spinnerCourse.setSelection(0); spinnerDepartment.setSelection(0)
        tvSyncStatus.visibility = View.GONE
        etFullName.requestFocus()
    }
}
