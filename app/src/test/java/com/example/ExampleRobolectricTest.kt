package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppRole
import com.example.data.model.JobCategory
import com.example.viewmodel.DailyCrewViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DAILYCREW", appName)
    }

    @Test
    fun `test role switching and job application`() {
        val viewModel = DailyCrewViewModel()

        // Default role is Worker
        assertEquals(AppRole.WORKER, viewModel.uiState.value.role)

        // Switch to Owner
        viewModel.switchRole(AppRole.OWNER)
        assertEquals(AppRole.OWNER, viewModel.uiState.value.role)

        // Switch back to Worker
        viewModel.switchRole(AppRole.WORKER)
        assertEquals(AppRole.WORKER, viewModel.uiState.value.role)

        // Apply for job
        val firstJob = viewModel.jobs.value.first()
        viewModel.applyForJob(firstJob.id)

        // Verify applicant added
        val applicant = viewModel.applicants.value.firstOrNull { it.jobId == firstJob.id }
        assertTrue(applicant != null)
    }

    @Test
    fun `test worker checkin and checkout workflow`() {
        val viewModel = DailyCrewViewModel()

        viewModel.performWorkerCheckIn()
        assertTrue(viewModel.workerProfile.value.isCheckedIn)

        viewModel.performWorkerCheckOut()
        assertTrue(!viewModel.workerProfile.value.isCheckedIn)
    }

    @Test
    fun `test notification service channels and dispatching`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val service = com.example.notification.DailyCrewNotificationService(application)

        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // Verify notification channels are created
        val workerChannel = notificationManager.getNotificationChannel(com.example.notification.DailyCrewNotificationService.CHANNEL_WORKER_JOBS)
        val ownerChannel = notificationManager.getNotificationChannel(com.example.notification.DailyCrewNotificationService.CHANNEL_OWNER_EVENTS)

        assertTrue(workerChannel != null)
        assertEquals("Nearby Job Alerts", workerChannel.name)
        assertTrue(ownerChannel != null)
        assertEquals("Incoming Applications & Check-Ins", ownerChannel.name)

        // Grant permission in Robolectric
        org.robolectric.Shadows.shadowOf(application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        assertTrue(service.hasPermission())

        // Test dispatching alerts
        val viewModel = DailyCrewViewModel()
        viewModel.setNotificationService(service)

        val sampleJob = viewModel.jobs.value.first()
        val jobNotifSent = service.notifyNewJobAlert(sampleJob)
        assertTrue(jobNotifSent)

        val sampleApp = viewModel.applicants.value.first()
        val appNotifSent = service.notifyNewApplicantAlert(sampleApp, sampleApp.jobTitle)
        assertTrue(appNotifSent)

        val checkInNotifSent = service.notifyWorkerCheckInAlert(
            workerName = "Raju Kumar",
            jobTitle = "Catering Steward",
            venueName = "Royal Banquet",
            time = "5:00 PM"
        )
        assertTrue(checkInNotifSent)
    }

    @Test
    fun `test standby mode and emergency hiring broadcast`() {
        val viewModel = DailyCrewViewModel()

        val initialStandby = viewModel.workerProfile.value.isAvailableNow
        viewModel.toggleWorkerStandby()
        assertEquals(!initialStandby, viewModel.workerProfile.value.isAvailableNow)

        // Post emergency 30-min hiring
        val initialJobCount = viewModel.jobs.value.size
        viewModel.postEmergency30MinHiring(
            title = "Urgent Kitchen Steward Needed",
            category = JobCategory.HOTEL,
            wageAmount = 850.0,
            workersCount = 2
        )
        val updatedJobs = viewModel.jobs.value
        assertEquals(initialJobCount + 1, updatedJobs.size)
        val emergencyJob = updatedJobs.first()
        assertTrue(emergencyJob.isEmergency30Min)
        assertEquals(JobCategory.HOTEL, emergencyJob.category)
        assertEquals(850.0, emergencyJob.wageAmount, 0.01)
    }

    @Test
    fun `test admin dispute resolution and banning`() {
        val viewModel = DailyCrewViewModel()

        // Test dispute resolution
        val activeDispute = viewModel.disputes.value.first()
        viewModel.resolveDispute(
            disputeId = activeDispute.id,
            ruling = "Refunded ₹800 to worker after verifying GPS arrival log.",
            releasePayment = true
        )
        val resolved = viewModel.disputes.value.first { it.id == activeDispute.id }
        assertEquals("Resolved - Payment Released", resolved.status)

        // Test user ban
        val bannedUser = "Violator Hotel 101"
        viewModel.banUser(bannedUser, "Multiple fake job cancellations")
        assertTrue(viewModel.bannedUsers.value.any { it.contains(bannedUser) })
    }

    @Test
    fun `test super admin platform settings update`() {
        val viewModel = DailyCrewViewModel()

        val newSettings = com.example.data.model.PlatformGlobalSettings(
            securityDepositPerJob = 750.0,
            commissionRatePercent = 7.5,
            emergencyHiringRadiusKm = 8.0
        )
        viewModel.updatePlatformSettings(newSettings)
        val settings = viewModel.platformSettings.value
        assertEquals(750.0, settings.securityDepositPerJob, 0.01)
        assertEquals(7.5, settings.commissionRatePercent, 0.01)
        assertEquals(8.0, settings.emergencyHiringRadiusKm, 0.01)
    }

    @Test
    fun `test brand logo drawables and color tokens resolve properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val brandLogo = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_dailycrew_brand_logo)
        val heroLogo = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_dailycrew_logo_hero)
        assertTrue("DailyCrew brand logo drawable should be non-null", brandLogo != null)
        assertTrue("DailyCrew hero logo drawable should be non-null", heroLogo != null)

        // Verify brand color constants
        assertEquals(0xFF0D1B2A, com.example.ui.theme.BrandDeepBlue.value.toLong().shr(32).let { 
            // In Compose Color, value is packed ULong ARGB
            (com.example.ui.theme.BrandDeepBlue.value shr 32).toLong()
        })
    }
}

