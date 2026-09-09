package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.model.AppRole
import com.example.notification.DailyCrewNotificationService
import com.example.ui.MainShellView
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DailyCrewViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DailyCrewViewModel by viewModels()
    private lateinit var notificationService: DailyCrewNotificationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        notificationService = DailyCrewNotificationService(applicationContext)
        viewModel.setNotificationService(notificationService)

        handleNotificationIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainShellView(
                    viewModel = viewModel,
                    notificationService = notificationService
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val targetRole = intent.getStringExtra("target_role")
        val jobId = intent.getStringExtra("job_id")
        val applicantId = intent.getStringExtra("applicant_id")

        if (targetRole == "OWNER") {
            viewModel.switchRole(AppRole.OWNER)
            if (applicantId != null) {
                viewModel.setBottomNavIndex(2) // Applicants tab
            }
        } else if (targetRole == "WORKER") {
            viewModel.switchRole(AppRole.WORKER)
            if (jobId != null) {
                val targetJob = viewModel.jobs.value.firstOrNull { it.id == jobId }
                if (targetJob != null) {
                    viewModel.openJobDetails(targetJob)
                }
            }
        }
    }
}

