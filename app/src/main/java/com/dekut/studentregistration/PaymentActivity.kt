package com.dekut.studentregistration

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.api.PaymentRequest
import com.dekut.studentregistration.api.RetrofitClient
import com.dekut.studentregistration.api.StatusRequest
import com.dekut.studentregistration.models.Payment
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.utils.NetworkUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.json.JSONObject

class PaymentActivity : AppCompatActivity() {

    private lateinit var tvStudentInfo: TextView
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var etAmount: TextInputEditText
    private lateinit var btnPayNow: MaterialButton
    private lateinit var btnClear: MaterialButton
    private lateinit var cardPaymentStatus: MaterialCardView
    private lateinit var tvPaymentStatus: TextView
    private lateinit var tvTransactionRef: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRepo: FirebaseRepository

    private var checkoutRequestID: String? = null
    private var currentStudentRegNumber: String = ""
    private var currentStudentUid: String = ""
    private var currentStudentDbKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        auth = FirebaseAuth.getInstance()
        firebaseRepo = FirebaseRepository.getInstance()

        initializeViews()
        loadCurrentStudent()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvStudentInfo = findViewById(R.id.tvStudentInfo)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etAmount = findViewById(R.id.etAmount)
        btnPayNow = findViewById(R.id.btnPayNow)
        btnClear = findViewById(R.id.btnClear)
        cardPaymentStatus = findViewById(R.id.cardPaymentStatus)
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus)
        tvTransactionRef = findViewById(R.id.tvTransactionRef)
    }

    private fun loadCurrentStudent() {
        val uid = auth.currentUser?.uid ?: return
        currentStudentUid = uid
        lifecycleScope.launch {
            val student = firebaseRepo.getStudentByUid(uid)
            if (student != null) {
                currentStudentRegNumber = student.registrationNumber
                currentStudentDbKey = student.id
                tvStudentInfo.text = "Student: ${student.fullName}\nReg: ${student.registrationNumber}"
            }
        }
    }

    private fun setupClickListeners() {
        btnPayNow.setOnClickListener { if (validateInput()) initiatePayment() }
        btnClear.setOnClickListener { clearForm() }
    }

    private fun validateInput(): Boolean {
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()
        when {
            phoneNumber.isEmpty() || phoneNumber.length < 12 -> {
                etPhoneNumber.error = "Enter valid phone number (254...)"; etPhoneNumber.requestFocus(); return false
            }
            !phoneNumber.startsWith("254") -> {
                etPhoneNumber.error = "Phone must start with 254"; etPhoneNumber.requestFocus(); return false
            }
            amountStr.isEmpty() -> {
                etAmount.error = getString(R.string.field_required); etAmount.requestFocus(); return false
            }
            amountStr.toDoubleOrNull() == null || amountStr.toDouble() <= 0 -> {
                etAmount.error = "Enter valid amount"; etAmount.requestFocus(); return false
            }
        }
        return true
    }

    private fun initiatePayment() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("Internet connection required for M-Pesa payment"); return
        }
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val amount = etAmount.text.toString().toDouble()
        AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage("Pay KES ${String.format("%.2f", amount)}\n\nM-Pesa prompt will be sent to $phoneNumber")
            .setPositiveButton("Proceed") { _, _ -> processPayment(phoneNumber, amount) }
            .setNegativeButton("Cancel", null).show()
    }

    private fun processPayment(phoneNumber: String, amount: Double) {
        btnPayNow.isEnabled = false
        btnPayNow.text = "Processing..."
        cardPaymentStatus.visibility = View.VISIBLE
        tvPaymentStatus.text = "Initiating M-Pesa payment..."
        tvTransactionRef.text = ""

        lifecycleScope.launch {
            try {
                val paymentRequest = PaymentRequest(
                    phoneNumber = phoneNumber,
                    amount = amount,
                    accountReference = currentStudentRegNumber.ifEmpty { "DEKUT" },
                    transactionDesc = "DeKUT Fees Payment"
                )
                val response = RetrofitClient.mpesaApi.initiatePayment(paymentRequest)

                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    checkoutRequestID = body.checkoutRequestID
                    tvPaymentStatus.text = "✓ M-Pesa prompt sent!\n\nEnter your M-Pesa PIN on your phone."
                    tvPaymentStatus.setTextColor(getColor(R.color.status_info))
                    tvTransactionRef.visibility = View.GONE
                    savePendingPayment(phoneNumber, amount, body.checkoutRequestID ?: "")
                    startPaymentStatusPolling(phoneNumber, amount)
                } else {
                    val rawErrorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    val errorMessage = response.body()?.message
                        ?: parseJsonErrorMessage(rawErrorBody)
                        ?: "Failed to initiate payment (HTTP ${response.code()}). Backend server may be offline."
                    tvPaymentStatus.text = "❌ $errorMessage"
                    tvPaymentStatus.setTextColor(getColor(R.color.status_error))
                    tvTransactionRef.visibility = View.GONE
                    showError(errorMessage)
                    resetPaymentUI()
                }
            } catch (e: Exception) {
                Log.e("PaymentActivity", "Payment error", e)
                val errorMessage = when {
                    e is java.net.UnknownHostException -> "Cannot reach payment server."
                    e is java.net.SocketTimeoutException -> "Connection timeout."
                    else -> "Error: ${e.message}"
                }
                tvPaymentStatus.text = "❌ $errorMessage"
                tvPaymentStatus.setTextColor(getColor(R.color.status_error))
                tvTransactionRef.visibility = View.GONE
                showError(errorMessage)
                resetPaymentUI()
            }
        }
    }

    private fun resetPaymentUI() {
        btnPayNow.isEnabled = true
        btnPayNow.text = "Pay Now"
    }

    private fun savePendingPayment(phoneNumber: String, amount: Double, checkoutId: String) {
        lifecycleScope.launch {
            try {
                firebaseRepo.savePayment(Payment(
                    uid = currentStudentUid,
                    studentId = currentStudentRegNumber,
                    phoneNumber = phoneNumber,
                    amount = amount,
                    transactionReference = checkoutId,
                    paymentStatus = "PENDING"
                ))
            } catch (e: Exception) {
                Log.e("PaymentActivity", "Error saving pending payment", e)
            }
        }
    }

    private fun startPaymentStatusPolling(phoneNumber: String, amount: Double) {
        var attempts = 0
        val maxAttempts = 15
        val handler = Handler(Looper.getMainLooper())

        fun pollStatus() {
            attempts++
            if (attempts > maxAttempts) {
                tvPaymentStatus.text = "⏱️ Verification timed out.\nIf your payment went through, check your M-Pesa SMS."
                tvPaymentStatus.setTextColor(getColor(R.color.status_pending))
                resetPaymentUI()
                return
            }
            checkoutRequestID?.let { requestId ->
                lifecycleScope.launch {
                    try {
                        val statusResponse = RetrofitClient.mpesaApi.checkPaymentStatus(StatusRequest(requestId))
                        if (statusResponse.isSuccessful && statusResponse.body()?.success == true) {
                            val body = statusResponse.body()!!
                            val code = body.resultCode
                            val desc = body.resultDesc

                            when {
                                code == "0" -> handleSuccessfulPayment(phoneNumber, amount, body.transactionId ?: requestId)
                                code == "1032" || desc.contains("cancel", ignoreCase = true) -> {
                                    tvPaymentStatus.text = "❌ Payment cancelled by user."
                                    tvPaymentStatus.setTextColor(getColor(R.color.status_error))
                                    resetPaymentUI()
                                }
                                code == "1" || code.contains("500") || desc.contains("process", ignoreCase = true) || desc.contains("pending", ignoreCase = true) || desc.contains("exist", ignoreCase = true) -> {
                                    tvPaymentStatus.text = "⏳ Waiting for M-Pesa PIN entry (Checking $attempts/$maxAttempts)..."
                                    tvPaymentStatus.setTextColor(getColor(R.color.status_info))
                                    handler.postDelayed({ pollStatus() }, 5000)
                                }
                                else -> {
                                    tvPaymentStatus.text = "❌ M-Pesa Error: $desc"
                                    tvPaymentStatus.setTextColor(getColor(R.color.status_error))
                                    resetPaymentUI()
                                }
                            }
                        } else {
                            tvPaymentStatus.text = "⏳ Processing M-Pesa payment..."
                            handler.postDelayed({ pollStatus() }, 5000)
                        }
                    } catch (e: Exception) {
                        Log.e("PaymentActivity", "Status check error", e)
                        handler.postDelayed({ pollStatus() }, 5000)
                    }
                }
            }
        }
        handler.postDelayed({ pollStatus() }, 3000)
    }

    private fun handleSuccessfulPayment(phoneNumber: String, amount: Double, transactionRef: String) {
        lifecycleScope.launch {
            try {
                firebaseRepo.savePayment(Payment(
                    uid = currentStudentUid,
                    studentId = currentStudentRegNumber,
                    phoneNumber = phoneNumber,
                    amount = amount,
                    transactionReference = transactionRef,
                    paymentStatus = "SUCCESS"
                ))
                if (currentStudentDbKey.isNotEmpty()) {
                    firebaseRepo.updateStudentPaymentStatus(currentStudentDbKey, true)
                    firebaseRepo.updateStudentRegistrationStatus(currentStudentDbKey, "CONFIRMED")
                }
                tvPaymentStatus.text = "✅ Payment Successful!\n\nRegistration CONFIRMED.\nAmount: KES ${String.format("%.2f", amount)}"
                tvPaymentStatus.setTextColor(getColor(R.color.status_success))
                tvTransactionRef.visibility = View.VISIBLE
                tvTransactionRef.text = "M-Pesa Ref: $transactionRef"
                showSuccess("Payment successful! Registration confirmed.")
                Handler(Looper.getMainLooper()).postDelayed({ clearForm() }, 4000)
                resetPaymentUI()
            } catch (e: Exception) {
                android.util.Log.e("PaymentActivity", "Error updating payment", e)
                tvPaymentStatus.text = "⚠️ Payment confirmed. Ref: $transactionRef\nContact support if status not updated."
                tvPaymentStatus.setTextColor(getColor(R.color.status_pending))
                resetPaymentUI()
            }
        }
    }

    private fun parseJsonErrorMessage(jsonStr: String?): String? {
        if (jsonStr.isNullOrEmpty()) return null
        return try {
            val jsonObj = JSONObject(jsonStr)
            val msg = jsonObj.optString("message")
            val err = jsonObj.optString("error")
            when {
                msg.isNotEmpty() -> msg
                err.isNotEmpty() -> err
                else -> null
            }
        } catch (_: Exception) {
            if (jsonStr.contains("ngrok", ignoreCase = true)) {
                "Backend tunnel (ngrok) is offline or expired."
            } else null
        }
    }

    private fun showSuccess(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.status_success)).show()
    private fun showError(msg: String) = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.status_error)).show()

    private fun clearForm() {
        etPhoneNumber.text?.clear(); etAmount.text?.clear()
        cardPaymentStatus.visibility = View.GONE
        tvPaymentStatus.setTextColor(getColor(R.color.text_primary))
    }
}
