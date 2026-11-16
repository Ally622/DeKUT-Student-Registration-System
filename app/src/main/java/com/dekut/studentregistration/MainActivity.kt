package com.dekut.studentregistration

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dekut.studentregistration.repository.OfflineRepository
import com.dekut.studentregistration.utils.NetworkUtils
import com.dekut.studentregistration.utils.SyncManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var offlineRepo: OfflineRepository
    private lateinit var syncManager: SyncManager
    
    private lateinit var syncStatusCard: MaterialCardView
    private lateinit var cardRegistration: MaterialCardView
    private lateinit var cardLibrary: MaterialCardView
    private lateinit var cardHostel: MaterialCardView
    private lateinit var cardPayment: MaterialCardView
    private lateinit var cardMyDetails: MaterialCardView
    private lateinit var cardAdmin: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        offlineRepo = OfflineRepository(this)
        syncManager = SyncManager(this)

        initializeViews()
        setupClickListeners()
        observeNetworkStatus()
        checkPendingSync()
    }

    private fun initializeViews() {
        syncStatusCard = findViewById(R.id.syncStatusCard)
        cardRegistration = findViewById(R.id.cardRegistration)
        cardLibrary = findViewById(R.id.cardLibrary)
        cardHostel = findViewById(R.id.cardHostel)
        cardPayment = findViewById(R.id.cardPayment)
        cardMyDetails = findViewById(R.id.cardMyDetails)
        cardAdmin = findViewById(R.id.cardAdmin)
    }

    private fun setupClickListeners() {
        cardRegistration.setOnClickListener {
            startActivity(Intent(this, StudentRegistrationActivity::class.java))
        }

        cardLibrary.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }

        cardHostel.setOnClickListener {
            startActivity(Intent(this, HostelActivity::class.java))
        }

        cardPayment.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        cardMyDetails.setOnClickListener {
            // TODO: Implement student details view
            Toast.makeText(this, "View your registration details", Toast.LENGTH_SHORT).show()
        }

        cardAdmin.setOnClickListener {
            startActivity(Intent(this, StudentsActivity::class.java))
        }
    }

    private fun observeNetworkStatus() {
        NetworkUtils.observeNetworkConnectivity(this)
            .onEach { isConnected ->
                if (isConnected && offlineRepo.hasPendingData()) {
                    syncPendingData()
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun checkPendingSync() {
        if (offlineRepo.hasPendingData()) {
            syncStatusCard.visibility = View.VISIBLE
            if (NetworkUtils.isNetworkAvailable(this)) {
                syncPendingData()
            }
        }
    }

    private fun syncPendingData() {
        lifecycleScope.launch {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Syncing pending data...",
                Snackbar.LENGTH_SHORT
            ).show()

            syncManager.syncPendingData { success ->
                runOnUiThread {
                    if (success) {
                        syncStatusCard.visibility = View.GONE
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "All data synced successfully",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Some data failed to sync. Will retry later.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPendingSync()
    }
}
