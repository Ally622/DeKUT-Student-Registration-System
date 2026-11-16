package com.dekut.studentregistration.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MpesaApi {
    
    @Headers("Content-Type: application/json")
    @POST("initiate-payment")
    suspend fun initiatePayment(@Body request: PaymentRequest): Response<PaymentResponse>
    
    @Headers("Content-Type: application/json")
    @POST("check-payment-status")
    suspend fun checkPaymentStatus(@Body request: StatusRequest): Response<StatusResponse>
}

data class PaymentRequest(
    val phoneNumber: String,
    val amount: Double,
    val accountReference: String,
    val transactionDesc: String
)

data class PaymentResponse(
    val success: Boolean,
    val message: String,
    val checkoutRequestID: String? = null,
    val merchantRequestID: String? = null
)

data class StatusRequest(
    val checkoutRequestID: String
)

data class StatusResponse(
    val success: Boolean,
    val resultCode: String,
    val resultDesc: String,
    val transactionId: String? = null
)
