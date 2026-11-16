package com.dekut.studentregistration.utils

import android.content.Context
import android.util.Log
import com.dekut.studentregistration.repository.FirebaseRepository
import com.dekut.studentregistration.repository.OfflineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncManager(private val context: Context) {
    
    private val firebaseRepo = FirebaseRepository.getInstance()
    private val offlineRepo = OfflineRepository(context)
    private val TAG = "SyncManager"
    
    fun syncPendingData(onComplete: (Boolean) -> Unit = {}) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "No network available, skipping sync")
            onComplete(false)
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var success = true
                
                // Sync students
                val pendingStudents = offlineRepo.getPendingStudents()
                    .filter { it.syncStatus == "PENDING" }
                
                for (student in pendingStudents) {
                    val result = firebaseRepo.saveStudent(student)
                    if (result.isSuccess) {
                        offlineRepo.markStudentAsSynced(student.id)
                        Log.d(TAG, "Synced student: ${student.registrationNumber}")
                    } else {
                        success = false
                        Log.e(TAG, "Failed to sync student: ${student.registrationNumber}")
                    }
                }
                
                // Sync library requests
                val pendingLibrary = offlineRepo.getPendingLibraryRequests()
                    .filter { it.syncStatus == "PENDING" }
                
                for (request in pendingLibrary) {
                    val result = firebaseRepo.saveLibraryRequest(request)
                    if (result.isSuccess) {
                        offlineRepo.removeLibraryRequest(request.id)
                        Log.d(TAG, "Synced library request: ${request.id}")
                    } else {
                        success = false
                        Log.e(TAG, "Failed to sync library request: ${request.id}")
                    }
                }
                
                // Sync hostel requests
                val pendingHostel = offlineRepo.getPendingHostelRequests()
                    .filter { it.syncStatus == "PENDING" }
                
                for (request in pendingHostel) {
                    val result = firebaseRepo.saveHostelRequest(request)
                    if (result.isSuccess) {
                        offlineRepo.removeHostelRequest(request.id)
                        Log.d(TAG, "Synced hostel request: ${request.id}")
                    } else {
                        success = false
                        Log.e(TAG, "Failed to sync hostel request: ${request.id}")
                    }
                }
                
                // Sync payments
                val pendingPayments = offlineRepo.getPendingPayments()
                    .filter { it.syncStatus == "PENDING" }
                
                for (payment in pendingPayments) {
                    val result = firebaseRepo.savePayment(payment)
                    if (result.isSuccess) {
                        offlineRepo.removePayment(payment.id)
                        Log.d(TAG, "Synced payment: ${payment.id}")
                    } else {
                        success = false
                        Log.e(TAG, "Failed to sync payment: ${payment.id}")
                    }
                }
                
                onComplete(success)
            } catch (e: Exception) {
                Log.e(TAG, "Sync error: ${e.message}")
                onComplete(false)
            }
        }
    }
}
