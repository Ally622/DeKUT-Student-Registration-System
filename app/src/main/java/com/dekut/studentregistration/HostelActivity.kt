package com.dekut.studentregistration

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.models.HostelRequest
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.repository.OfflineRepository
import com.dekut.studentregistration.utils.NetworkUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HostelActivity : AppCompatActivity() {

    private lateinit var tvStudentInfo: TextView
    private lateinit var spinnerHostelLocation: Spinner
    private lateinit var spinnerRoomType: Spinner
    private lateinit var rgSharable: RadioGroup
    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnClear: MaterialButton
    private lateinit var progressBar: android.widget.ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRepo: FirebaseRepository
    private lateinit var offlineRepo: OfflineRepository

    private var currentStudentRegNumber: String = ""
    private var currentStudentUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hostel)

        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()
        offlineRepo = OfflineRepository(this)

        initializeViews()
        loadCurrentStudent()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvStudentInfo = findViewById(R.id.tvStudentInfo)
        spinnerHostelLocation = findViewById(R.id.spinnerHostelLocation)
        spinnerRoomType = findViewById(R.id.spinnerRoomType)
        rgSharable = findViewById(R.id.rgSharable)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnClear = findViewById(R.id.btnClear)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun loadCurrentStudent() {
        val uid = auth.currentUser?.uid ?: return
        currentStudentUid = uid
        lifecycleScope.launch {
            val student = firebaseRepo.getStudentByUid(uid)
            if (student != null) {
                currentStudentRegNumber = student.registrationNumber
                tvStudentInfo.text = "Student: ${student.fullName} (${student.registrationNumber})"
            }
        }
    }

    private fun setupClickListeners() {
        btnSubmit.setOnClickListener { if (validateInput()) submitRequest() }
        btnClear.setOnClickListener { clearForm() }
    }

    private fun validateInput(): Boolean {
        when {
            spinnerHostelLocation.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Please select hostel location", Toast.LENGTH_SHORT).show(); return false
            }
            spinnerRoomType.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Please select room type", Toast.LENGTH_SHORT).show(); return false
            }
            rgSharable.checkedRadioButtonId == -1 -> {
                Toast.makeText(this, "Please select if room is sharable", Toast.LENGTH_SHORT).show(); return false
            }
        }
        return true
    }

    private fun submitRequest() {
        val sharable = if (findViewById<RadioButton>(R.id.rbYes).isChecked) "Yes" else "No"
        val request = HostelRequest(
            uid = currentStudentUid,
            studentId = currentStudentRegNumber,
            hostelLocation = spinnerHostelLocation.selectedItem.toString(),
            roomType = spinnerRoomType.selectedItem.toString(),
            sharable = sharable
        )

        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val isOnline = NetworkUtils.isNetworkAvailable(this@HostelActivity)
                if (isOnline) {
                    val result = firebaseRepo.saveHostelRequest(request)
                    if (result.isSuccess) {
                        showSuccess("✅ Hostel request submitted!")
                        clearForm()
                    } else {
                        val err = result.exceptionOrNull()?.message ?: "Unknown error"
                        showError("Firebase error: $err\nSaved offline.")
                        offlineRepo.saveHostelRequestOffline(request)
                        clearForm()
                    }
                } else {
                    offlineRepo.saveHostelRequestOffline(request)
                    showSuccess("📱 Saved offline. Will sync when online.")
                    clearForm()
                }
            } catch (e: Exception) {
                android.util.Log.e("HostelActivity", "Submit error", e)
                offlineRepo.saveHostelRequestOffline(request)
                showSuccess("📱 Saved offline. Will sync when online.")
                clearForm()
            } finally {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Request"
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showSuccess(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.status_success)).show()
    private fun showError(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.status_error)).show()

    private fun clearForm() {
        spinnerHostelLocation.setSelection(0)
        spinnerRoomType.setSelection(0)
        rgSharable.clearCheck()
    }
}
