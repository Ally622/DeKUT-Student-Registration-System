package com.dekut.studentregistration.repository

import android.content.Context
import android.content.SharedPreferences
import com.dekut.studentregistration.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OfflineRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "DeKUTOfflineData",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    // STUDENT OPERATIONS
    fun saveStudentOffline(student: Student): String {
        val students = getPendingStudents().toMutableList()
        
        // Generate ID if not exists
        if (student.id.isEmpty()) {
            student.id = "offline_${System.currentTimeMillis()}_${students.size}"
        }
        student.syncStatus = "PENDING"
        
        // Check for duplicates
        val existingIndex = students.indexOfFirst { it.registrationNumber == student.registrationNumber }
        if (existingIndex >= 0) {
            students[existingIndex] = student
        } else {
            students.add(student)
        }
        
        val json = gson.toJson(students)
        prefs.edit().putString(KEY_STUDENTS, json).apply()
        
        return student.id
    }
    
    fun getPendingStudents(): List<Student> {
        val json = prefs.getString(KEY_STUDENTS, "[]") ?: "[]"
        val type = object : TypeToken<List<Student>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun getStudentById(studentId: String): Student? {
        return getPendingStudents().find { it.id == studentId }
    }
    
    fun getStudentByRegistrationNumber(regNumber: String): Student? {
        return getPendingStudents().find { it.registrationNumber == regNumber }
    }
    
    fun markStudentAsSynced(studentId: String) {
        val students = getPendingStudents().toMutableList()
        val index = students.indexOfFirst { it.id == studentId }
        if (index >= 0) {
            students[index].syncStatus = "SYNCED"
            val json = gson.toJson(students)
            prefs.edit().putString(KEY_STUDENTS, json).apply()
        }
    }
    
    fun removeStudent(studentId: String) {
        val students = getPendingStudents().toMutableList()
        students.removeAll { it.id == studentId }
        val json = gson.toJson(students)
        prefs.edit().putString(KEY_STUDENTS, json).apply()
    }
    
    // LIBRARY OPERATIONS
    fun saveLibraryRequestOffline(request: LibraryRequest): String {
        val requests = getPendingLibraryRequests().toMutableList()
        
        if (request.id.isEmpty()) {
            request.id = "offline_lib_${System.currentTimeMillis()}_${requests.size}"
        }
        request.syncStatus = "PENDING"
        
        requests.add(request)
        val json = gson.toJson(requests)
        prefs.edit().putString(KEY_LIBRARY, json).apply()
        
        return request.id
    }
    
    fun getPendingLibraryRequests(): List<LibraryRequest> {
        val json = prefs.getString(KEY_LIBRARY, "[]") ?: "[]"
        val type = object : TypeToken<List<LibraryRequest>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun removeLibraryRequest(requestId: String) {
        val requests = getPendingLibraryRequests().toMutableList()
        requests.removeAll { it.id == requestId }
        val json = gson.toJson(requests)
        prefs.edit().putString(KEY_LIBRARY, json).apply()
    }
    
    // HOSTEL OPERATIONS
    fun saveHostelRequestOffline(request: HostelRequest): String {
        val requests = getPendingHostelRequests().toMutableList()
        
        if (request.id.isEmpty()) {
            request.id = "offline_hostel_${System.currentTimeMillis()}_${requests.size}"
        }
        request.syncStatus = "PENDING"
        
        requests.add(request)
        val json = gson.toJson(requests)
        prefs.edit().putString(KEY_HOSTEL, json).apply()
        
        return request.id
    }
    
    fun getPendingHostelRequests(): List<HostelRequest> {
        val json = prefs.getString(KEY_HOSTEL, "[]") ?: "[]"
        val type = object : TypeToken<List<HostelRequest>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun removeHostelRequest(requestId: String) {
        val requests = getPendingHostelRequests().toMutableList()
        requests.removeAll { it.id == requestId }
        val json = gson.toJson(requests)
        prefs.edit().putString(KEY_HOSTEL, json).apply()
    }
    
    // PAYMENT OPERATIONS
    fun savePaymentOffline(payment: Payment): String {
        val payments = getPendingPayments().toMutableList()
        
        if (payment.id.isEmpty()) {
            payment.id = "offline_pay_${System.currentTimeMillis()}_${payments.size}"
        }
        payment.syncStatus = "PENDING"
        
        payments.add(payment)
        val json = gson.toJson(payments)
        prefs.edit().putString(KEY_PAYMENTS, json).apply()
        
        return payment.id
    }
    
    fun getPendingPayments(): List<Payment> {
        val json = prefs.getString(KEY_PAYMENTS, "[]") ?: "[]"
        val type = object : TypeToken<List<Payment>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun removePayment(paymentId: String) {
        val payments = getPendingPayments().toMutableList()
        payments.removeAll { it.id == paymentId }
        val json = gson.toJson(payments)
        prefs.edit().putString(KEY_PAYMENTS, json).apply()
    }
    
    // Check if there's any pending data
    fun hasPendingData(): Boolean {
        return getPendingStudents().any { it.syncStatus == "PENDING" } ||
                getPendingLibraryRequests().any { it.syncStatus == "PENDING" } ||
                getPendingHostelRequests().any { it.syncStatus == "PENDING" } ||
                getPendingPayments().any { it.syncStatus == "PENDING" }
    }
    
    companion object {
        private const val KEY_STUDENTS = "pending_students"
        private const val KEY_LIBRARY = "pending_library"
        private const val KEY_HOSTEL = "pending_hostel"
        private const val KEY_PAYMENTS = "pending_payments"
    }
}
