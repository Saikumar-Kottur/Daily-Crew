package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppRole
import com.example.data.model.JobCategory
import com.example.viewmodel.DailyCrewViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    }

    @Test
    fun `test worker profile verification status, work history, and skill badges`() {
        val viewModel = DailyCrewViewModel()
        val profile = viewModel.workerProfile.value

        // Verification status assertions
        assertTrue("Worker should be fully verified", profile.isFullyVerified)
        assertTrue("Phone should be verified", profile.isPhoneVerified)
        assertTrue("Aadhaar should be verified", profile.isAadhaarVerified)
        assertTrue("Police clearance should be verified", profile.isPoliceVerified)
        assertTrue("Selfie face match should be verified", profile.isSelfieVerified)
        assertEquals(96, profile.trustScore)
        assertEquals(com.example.data.model.BadgeTier.PLATINUM, profile.badgeTier)

        // Work history assertions
        assertTrue("Worker should have completed shifts in history", profile.workHistory.isNotEmpty())
        val firstShift = profile.workHistory.first()
        assertEquals("Royal Feast Caterers", firstShift.businessName)
        assertEquals(5.0, firstShift.ratingGiven, 0.01)
        assertTrue("Shift should have positive supervisor review", firstShift.supervisorReview.isNotBlank())
        assertTrue("Shift should have verified checkout", firstShift.isVerifiedCheckout)

        // Skill badges assertions
        assertTrue("Worker should have accredited skill badges", profile.skillBadgesList.isNotEmpty())
        val cateringBadge = profile.skillBadgesList.first { it.name == "Catering Steward" }
        assertTrue("Catering Steward should be accredited", cateringBadge.isAccredited)
        assertTrue("Badge should have positive endorsements", cateringBadge.endorsements > 0)
    }

    @Test
    fun `test marketplace filtering by skills like catering and housekeeping and verification status`() {
        val viewModel = DailyCrewViewModel()
        val allApplicants = viewModel.applicants.value

        assertTrue("Should have initial marketplace applicants", allApplicants.isNotEmpty())

        // 1. Filter by 'Catering' skill
        val cateringWorkers = allApplicants.filter { applicant ->
            applicant.skills.any { it.contains("catering", ignoreCase = true) } ||
            applicant.role.contains("catering", ignoreCase = true)
        }
        assertTrue("Should find workers with catering skill", cateringWorkers.isNotEmpty())
        assertTrue("Ramesh Kumar should be in catering workers", cateringWorkers.any { it.workerName == "Ramesh Kumar" })

        // 2. Filter by 'Housekeeping' skill
        val housekeepingWorkers = allApplicants.filter { applicant ->
            applicant.skills.any { it.contains("housekeeping", ignoreCase = true) } ||
            applicant.role.contains("housekeeping", ignoreCase = true)
        }
        assertTrue("Should find workers with housekeeping skill", housekeepingWorkers.isNotEmpty())
        assertTrue("Anita Rao should be in housekeeping workers", housekeepingWorkers.any { it.workerName == "Anita Rao" })

        // 3. Filter by 'Verified' status
        val verifiedWorkers = allApplicants.filter { it.isVerified }
        val unverifiedWorkers = allApplicants.filter { !it.isVerified }
        assertTrue("Should have verified workers in marketplace", verifiedWorkers.isNotEmpty())
        assertTrue("Should have workers pending verification", unverifiedWorkers.isNotEmpty())
        assertTrue("Ramesh Kumar should be verified", verifiedWorkers.any { it.workerName == "Ramesh Kumar" })
        assertTrue("Rajesh Naik should be pending verification", unverifiedWorkers.any { it.workerName == "Rajesh Naik" })

        // 4. Combined Filter: 'Housekeeping' + 'Verified Only'
        val verifiedHousekeeping = allApplicants.filter { applicant ->
            applicant.isVerified && (
                applicant.skills.any { it.contains("housekeeping", ignoreCase = true) } ||
                applicant.role.contains("housekeeping", ignoreCase = true)
            )
        }
        assertTrue("Should find verified housekeeping workers", verifiedHousekeeping.isNotEmpty())
        assertTrue("Anita Rao should be verified housekeeping lead", verifiedHousekeeping.any { it.workerName == "Anita Rao" })
        // Rajesh Naik is housekeeping but unverified, so should NOT be in verifiedHousekeeping
        assertFalse("Unverified worker should not appear in verified housekeeping results", verifiedHousekeeping.any { it.workerName == "Rajesh Naik" })

        // 5. Combined Filter: 'Catering' + 'Verified Only'
        val verifiedCatering = allApplicants.filter { applicant ->
            applicant.isVerified && (
                applicant.skills.any { it.contains("catering", ignoreCase = true) } ||
                applicant.role.contains("catering", ignoreCase = true)
            )
        }
        assertTrue("Should find verified catering workers", verifiedCatering.isNotEmpty())
        assertTrue("Sunita Devi should be in verified catering workers", verifiedCatering.any { it.workerName == "Sunita Devi" })
    }

    @Test
    fun `test UPI and Bank Account withdrawals in wallet`() {
        val viewModel = DailyCrewViewModel()
        val initialBalance = viewModel.workerProfile.value.walletBalance
        assertTrue("Worker should have positive balance", initialBalance > 2000.0)

        // 1. Withdraw via UPI
        val upiAmount = 500.0
        viewModel.withdrawWallet(upiAmount, "UPI", "ramesh@okhdfcbank")
        val balanceAfterUpi = viewModel.workerProfile.value.walletBalance
        assertEquals(initialBalance - upiAmount, balanceAfterUpi, 0.01)

        val latestUpiTxn = viewModel.transactions.value.first()
        assertEquals("Instant UPI", latestUpiTxn.paymentMethod)
        assertEquals("ramesh@okhdfcbank", latestUpiTxn.businessOrWorker)

        // 2. Withdraw via Bank Account (IMPS)
        val bankAmount = 1000.0
        viewModel.withdrawWallet(bankAmount, "Bank Account", "HDFC Bank (A/C ***8234)")
        val balanceAfterBank = viewModel.workerProfile.value.walletBalance
        assertEquals(balanceAfterUpi - bankAmount, balanceAfterBank, 0.01)

        val latestBankTxn = viewModel.transactions.value.first()
        assertEquals("Bank IMPS Transfer", latestBankTxn.paymentMethod)
        assertEquals("Direct Bank IMPS Payout", latestBankTxn.title)
        assertEquals("HDFC Bank (A/C ***8234)", latestBankTxn.businessOrWorker)
    }
}


