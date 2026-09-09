package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DailyCrewRepository {

    private val _jobs = MutableStateFlow<List<JobPosting>>(initialJobs())
    val jobs: StateFlow<List<JobPosting>> = _jobs.asStateFlow()

    private val _workerProfile = MutableStateFlow(WorkerProfile())
    val workerProfile: StateFlow<WorkerProfile> = _workerProfile.asStateFlow()

    private val _businessProfile = MutableStateFlow(BusinessProfile())
    val businessProfile: StateFlow<BusinessProfile> = _businessProfile.asStateFlow()

    private val _applicants = MutableStateFlow<List<JobApplicant>>(initialApplicants())
    val applicants: StateFlow<List<JobApplicant>> = _applicants.asStateFlow()

    private val _transactions = MutableStateFlow<List<PayoutTransaction>>(initialTransactions())
    val transactions: StateFlow<List<PayoutTransaction>> = _transactions.asStateFlow()

    private val _disputes = MutableStateFlow<List<DisputeReport>>(initialDisputes())
    val disputes: StateFlow<List<DisputeReport>> = _disputes.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(initialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _platformSettings = MutableStateFlow(PlatformGlobalSettings())
    val platformSettings: StateFlow<PlatformGlobalSettings> = _platformSettings.asStateFlow()

    private val _bannedUsers = MutableStateFlow<List<String>>(emptyList())
    val bannedUsers: StateFlow<List<String>> = _bannedUsers.asStateFlow()

    // ---------------- Worker Actions ----------------

    fun toggleWorkerStandby(): Boolean {
        val nextVal = !_workerProfile.value.isAvailableNow
        _workerProfile.update { it.copy(isAvailableNow = nextVal) }
        val msg = if (nextVal) {
            "You are now LIVE on Standby! Nearby businesses can instantly hire you for 30-min shifts."
        } else {
            "Standby mode paused. You will not receive emergency dispatch alerts."
        }
        addNotification("Standby Mode: ${if (nextVal) "ACTIVE" else "OFF"}", msg, "ALERT")
        return nextVal
    }

    fun applyForJob(jobId: String): JobApplicant? {
        var applied = false
        _jobs.update { list ->
            list.map { job ->
                if (job.id == jobId) {
                    applied = true
                    job.copy(isApplied = true)
                } else job
            }
        }
        var createdApplicant: JobApplicant? = null
        if (applied) {
            val job = _jobs.value.firstOrNull { it.id == jobId }
            val worker = _workerProfile.value
            val newApp = JobApplicant(
                id = "app_${System.currentTimeMillis()}",
                jobId = jobId,
                jobTitle = job?.title ?: "Shift Worker",
                workerName = worker.name,
                role = job?.category?.label ?: "Steward",
                rating = worker.rating,
                jobsCompleted = worker.completedJobs,
                onTimeRate = worker.onTimeRate,
                trustScore = worker.trustScore,
                isVerified = worker.isPhoneVerified && worker.isSelfieVerified,
                status = ApplicantStatus.PENDING,
                appliedAt = "Just now",
                isStandbyReady = worker.isAvailableNow,
                distanceKm = job?.distanceKm ?: 1.0
            )
            createdApplicant = newApp
            _applicants.update { listOf(newApp) + it }
            addNotification("Application Submitted", "Your application for ${job?.title} at ${job?.businessName} is pending owner review.", "JOB")
        }
        return createdApplicant
    }

    fun performWorkerCheckIn(): Boolean {
        _workerProfile.update {
            it.copy(
                isCheckedIn = true,
                checkInTimestamp = "5:02 PM Today",
                activeShiftId = "1"
            )
        }
        addNotification("Geo-Fence Verified", "Checked in at Royal Feast Caterers (Banjara Hills). Shift timer active.", "CHECKIN")
        return true
    }

    fun performWorkerCheckOut(): Boolean {
        val worker = _workerProfile.value
        val shiftGross = 850.0
        val commissionRate = _platformSettings.value.commissionRatePercent / 100.0
        val platformFee = shiftGross * commissionRate
        val netPayout = shiftGross - platformFee

        _workerProfile.update {
            it.copy(
                isCheckedIn = false,
                checkInTimestamp = null,
                activeShiftId = null,
                completedJobs = it.completedJobs + 1,
                pendingPayout = it.pendingPayout + netPayout,
                xpPoints = it.xpPoints + 150
            )
        }
        addNotification("Shift Completed", "QR Check-Out confirmed. ₹${netPayout.toInt()} queued for instant escrow release (Fee: ₹${platformFee.toInt()}).", "PAYOUT")
        return true
    }

    fun withdrawWorkerWallet(amount: Double) {
        val currentBal = _workerProfile.value.walletBalance
        if (amount <= currentBal) {
            _workerProfile.update { it.copy(walletBalance = it.walletBalance - amount) }
            val txn = PayoutTransaction(
                id = "wth_${System.currentTimeMillis()}",
                title = "Instant UPI Bank Payout",
                businessOrWorker = "ICICI Bank (A/C ***4812)",
                date = "Today",
                grossAmount = amount,
                platformFee = 0.0,
                netAmount = amount,
                isCredit = false,
                paymentMethod = "Instant UPI",
                status = "Success (Instant Settlement)"
            )
            _transactions.update { listOf(txn) + it }
            addNotification("Withdrawal Successful", "₹${amount.toInt()} transferred to registered UPI handle instantly.", "PAYOUT")
        }
    }

    // ---------------- Owner Actions ----------------

    fun postNewJob(
        title: String,
        category: JobCategory,
        wageAmount: Double,
        workersCount: Int,
        dressCode: String,
        instructions: String,
        location: String,
        time: String,
        zone: String = "Uppal"
    ): JobPosting {
        val deposit = _platformSettings.value.securityDepositPerJob
        val commission = _platformSettings.value.commissionRatePercent
        val newJob = JobPosting(
            id = "job_${System.currentTimeMillis()}",
            title = title,
            businessName = _businessProfile.value.businessName,
            businessCategory = _businessProfile.value.category,
            wage = "₹${wageAmount.toInt()}",
            unit = "/ shift (8 hrs)",
            wageAmount = wageAmount,
            distanceKm = 0.8,
            time = time.ifBlank { "Tonight, 6:00 PM - 2:00 AM" },
            date = "Today",
            dressCode = dressCode.ifBlank { "Standard neat uniform / Closed shoes" },
            location = location.ifBlank { "Road No. 12, Banjara Hills, Hyderabad" },
            zone = zone,
            instructions = instructions.ifBlank { "Report directly to supervisor at venue gate." },
            workersRequired = workersCount,
            workersHired = 0,
            category = category,
            rating = _businessProfile.value.rating,
            isApplied = false,
            isUrgent = true,
            isEmergency30Min = false,
            securityDeposit = deposit,
            commissionPercent = commission
        )
        _jobs.update { listOf(newJob) + it }
        _businessProfile.update {
            it.copy(
                activeShiftCount = it.activeShiftCount + 1,
                escrowSecurityDepositBalance = it.escrowSecurityDepositBalance + deposit
            )
        }
        addNotification("Shift Published (Deposit Escrowed)", "Shift published. ₹${deposit.toInt()} anti-fake security deposit placed in refundable escrow.", "JOB")
        return newJob
    }

    fun postEmergency30MinHiring(
        title: String,
        category: JobCategory,
        wageAmount: Double,
        workersCount: Int
    ): JobPosting {
        val deposit = _platformSettings.value.securityDepositPerJob
        val emergencyJob = JobPosting(
            id = "emerg_${System.currentTimeMillis()}",
            title = "⚡ URGENT: $title (30-Min Arrival)",
            businessName = _businessProfile.value.businessName,
            businessCategory = _businessProfile.value.category,
            wage = "₹${wageAmount.toInt()}",
            unit = "/ shift (urgent)",
            wageAmount = wageAmount,
            distanceKm = 0.5,
            time = "Arrival within 30 Minutes! Shift starts immediately",
            date = "Today",
            dressCode = "Standard neat black attire",
            location = _businessProfile.value.address,
            zone = _businessProfile.value.zone,
            instructions = "Emergency Staffing: Report to supervisor immediately upon arrival. QR check-in at front counter.",
            workersRequired = workersCount,
            workersHired = 0,
            category = category,
            rating = _businessProfile.value.rating,
            isApplied = false,
            isUrgent = true,
            isEmergency30Min = true,
            securityDeposit = deposit,
            commissionPercent = _platformSettings.value.commissionRatePercent
        )
        _jobs.update { listOf(emergencyJob) + it }
        _businessProfile.update { it.copy(activeShiftCount = it.activeShiftCount + 1) }
        addNotification(
            "⚡ 30-Min Emergency Broadcast Sent",
            "Emergency dispatch alert sent to 12 nearby Standby Workers in ${_businessProfile.value.zone}.",
            "EMERGENCY"
        )
        return emergencyJob
    }

    fun hireApplicant(applicantId: String) {
        _applicants.update { list ->
            list.map { app ->
                if (app.id == applicantId) {
                    app.copy(status = ApplicantStatus.HIRED)
                } else app
            }
        }
        val app = _applicants.value.firstOrNull { it.id == applicantId }
        addNotification("Worker Hired", "${app?.workerName} hired for ${app?.jobTitle}. Shift confirmed.", "CHECKIN")
    }

    fun rejectApplicant(applicantId: String) {
        _applicants.update { list ->
            list.map { app ->
                if (app.id == applicantId) {
                    app.copy(status = ApplicantStatus.REJECTED)
                } else app
            }
        }
    }

    fun triggerOwnerAutoPayout(amount: Double) {
        val commission = _platformSettings.value.commissionRatePercent / 100.0
        val fee = amount * commission
        val netDisbursed = amount - fee

        val txn = PayoutTransaction(
            id = "txn_${System.currentTimeMillis()}",
            title = "Shift Auto-Payout Disbursed",
            businessOrWorker = "Disbursed to 3 Checked-in Workers via UPI",
            date = "Today",
            grossAmount = amount,
            platformFee = fee,
            netAmount = netDisbursed,
            isCredit = false,
            paymentMethod = "Razorpay / UPI",
            status = "Success (Instant Settlement)"
        )
        _transactions.update { listOf(txn) + it }
        _businessProfile.update {
            it.copy(
                totalPayoutsDisbursed = it.totalPayoutsDisbursed + amount,
                totalWorkersHired = it.totalWorkersHired + 3
            )
        }
        addNotification("Payouts Disbursed", "Instant UPI payout of ₹${netDisbursed.toInt()} successfully settled (Platform fee: ₹${fee.toInt()}).", "PAYOUT")
    }

    // ---------------- Dispute & Moderation ----------------

    fun fileDispute(
        reportedByRole: String,
        reporterName: String,
        target: String,
        reason: String,
        details: String
    ) {
        val disp = DisputeReport(
            id = "disp_${System.currentTimeMillis()}",
            reportedByRole = reportedByRole,
            reporterName = reporterName,
            targetName = target,
            reason = reason,
            details = details,
            evidenceLogs = listOf(
                "GPS Arrival Proof: Lat 17.4012, Lng 78.5582",
                "QR Scan Verification Timestamp: 05:02:14 PM",
                "Attendance Ledger Hash: verified_dc_${System.currentTimeMillis() % 10000}"
            ),
            status = "Under Review",
            timestamp = "Just now"
        )
        _disputes.update { listOf(disp) + it }
        addNotification("Dispute Filed", "Ticket #${disp.id.takeLast(6)} logged for '$reason'. Mediation team assigned.", "ALERT")
    }

    // ---------------- Admin & Super Admin Actions ----------------

    fun updatePlatformSettings(newSettings: PlatformGlobalSettings) {
        _platformSettings.value = newSettings
        addNotification("Settings Updated", "Platform commission set to ${newSettings.commissionRatePercent}%, Security deposit to ₹${newSettings.securityDepositPerJob.toInt()}.", "ALERT")
    }

    fun resolveDispute(disputeId: String, rulingText: String, releasePaymentToWorker: Boolean) {
        _disputes.update { list ->
            list.map { disp ->
                if (disp.id == disputeId) {
                    disp.copy(
                        status = if (releasePaymentToWorker) "Resolved - Payment Released" else "Resolved - Refunded to Business",
                        adminRuling = rulingText
                    )
                } else disp
            }
        }
        addNotification("Dispute Resolved", "Dispute ticket $disputeId has been officially adjudicated by Admin.", "ALERT")
    }

    fun toggleBusinessVerification(): Boolean {
        val newVal = !_businessProfile.value.isLocationVerified
        _businessProfile.update {
            it.copy(
                isLocationVerified = newVal,
                isGoogleVerified = newVal,
                isPhoneVerified = true
            )
        }
        return newVal
    }

    fun toggleWorkerVerification(): Boolean {
        val newVal = !_workerProfile.value.isSelfieVerified
        _workerProfile.update {
            it.copy(
                isSelfieVerified = newVal,
                isGoogleVerified = newVal,
                isPhoneVerified = true
            )
        }
        return newVal
    }

    fun banUser(userName: String, reason: String) {
        _bannedUsers.update { it + "$userName ($reason)" }
        addNotification("Account Action", "User $userName has been suspended: $reason", "ALERT")
    }

    fun addNotification(title: String, message: String, type: String) {
        val notif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            title = title,
            message = message,
            time = "Just now",
            type = type,
            isRead = false
        )
        _notifications.update { listOf(notif) + it }
    }

    // ---------------- Seed Data ----------------

    private fun initialJobs(): List<JobPosting> = listOf(
        JobPosting(
            id = "1",
            title = "Senior Catering Steward",
            businessName = "Royal Feast Caterers",
            businessCategory = "Banquet & Luxury Catering",
            wage = "₹850",
            unit = "/ shift (8 hrs)",
            wageAmount = 850.0,
            distanceKm = 1.2,
            time = "Today, 5:00 PM - 1:00 AM",
            date = "Today",
            dressCode = "Black Formal Shirt & Trousers, Black Leather Shoes",
            location = "Banjara Hills, Hyderabad",
            zone = "Uppal",
            instructions = "Report to Hall Captain at Main Stage Gate 2. Clean groomed appearance mandatory. Free dinner included.",
            workersRequired = 5,
            workersHired = 3,
            category = JobCategory.CATERING,
            rating = 4.8,
            experienceRequired = "1+ year in banquet / catering",
            isApplied = false,
            isUrgent = true,
            isEmergency30Min = false,
            securityDeposit = 500.0,
            commissionPercent = 6.5
        ),
        JobPosting(
            id = "2",
            title = "Kitchen Line Helper & Prep",
            businessName = "Spice Bazaar Restaurant",
            businessCategory = "Fine Dine Restaurant",
            wage = "₹700",
            unit = "/ shift (6 hrs)",
            wageAmount = 700.0,
            distanceKm = 2.8,
            time = "Tomorrow, 11:00 AM - 5:00 PM",
            date = "Tomorrow",
            dressCode = "Apron & Hairnet provided, Closed Non-slip Shoes",
            location = "Madhapur, Hyderabad",
            zone = "Habsiguda",
            instructions = "Vegetable prep, cleaning counters, and assist head chef during lunch rush.",
            workersRequired = 3,
            workersHired = 1,
            category = JobCategory.KITCHEN_HELPER,
            rating = 4.6,
            experienceRequired = "Basic knife skills & food hygiene",
            isApplied = false,
            isUrgent = false,
            isEmergency30Min = false,
            securityDeposit = 500.0,
            commissionPercent = 6.5
        ),
        JobPosting(
            id = "3",
            title = "VIP Event Host & Usher",
            businessName = "Apex Global Summit 2026",
            businessCategory = "Event Management",
            wage = "₹1,200",
            unit = "/ shift (8 hrs)",
            wageAmount = 1200.0,
            distanceKm = 3.4,
            time = "Tomorrow, 9:00 AM - 5:00 PM",
            date = "Tomorrow",
            dressCode = "Dark Navy Blazer or Formal Suit with ID badge",
            location = "HICC Novotel, Hitec City, Hyderabad",
            zone = "Tarnaka",
            instructions = "Guest registration scanning, delegate guide at auditorium entrance.",
            workersRequired = 8,
            workersHired = 5,
            category = JobCategory.EVENT,
            rating = 4.9,
            experienceRequired = "Fluent English & Hindi communication",
            isApplied = false,
            isUrgent = true,
            isEmergency30Min = false,
            securityDeposit = 500.0,
            commissionPercent = 6.5
        ),
        JobPosting(
            id = "4",
            title = "Hotel Housekeeping Attendant",
            businessName = "Marigold Grand Hotel",
            businessCategory = "Luxury Hotel",
            wage = "₹750",
            unit = "/ shift (8 hrs)",
            wageAmount = 750.0,
            distanceKm = 4.1,
            time = "Today, 2:00 PM - 10:00 PM",
            date = "Today",
            dressCode = "Comfortable Dark Pants, Uniform shirt provided",
            location = "Begumpet, Hyderabad",
            zone = "Nacharam",
            instructions = "Linen changes, room replenishment, and corridor sanitation.",
            workersRequired = 4,
            workersHired = 2,
            category = JobCategory.HOUSEKEEPING,
            rating = 4.7,
            experienceRequired = "Prior hotel or hospital cleaning experience",
            isApplied = false,
            isUrgent = false,
            isEmergency30Min = false,
            securityDeposit = 500.0,
            commissionPercent = 6.5
        ),
        JobPosting(
            id = "5",
            title = "Corporate Canteen Server",
            businessName = "TechPark Cafeteria Services",
            businessCategory = "Corporate Canteen",
            wage = "₹650",
            unit = "/ shift (6 hrs)",
            wageAmount = 650.0,
            distanceKm = 1.9,
            time = "Today, 12:00 PM - 6:00 PM",
            date = "Today",
            dressCode = "White Polo Shirt, Black Pants",
            location = "Mindspace Tech Park, Hyderabad",
            zone = "Uppal",
            instructions = "Buffet counter serving and tray clearing. Quick pace required.",
            workersRequired = 6,
            workersHired = 4,
            category = JobCategory.CANTEEN,
            rating = 4.5,
            experienceRequired = "Friendly customer attitude",
            isApplied = false,
            isUrgent = false,
            isEmergency30Min = false,
            securityDeposit = 500.0,
            commissionPercent = 6.5
        ),
        JobPosting(
            id = "6",
            title = "Peak Hours Counter Cashier",
            businessName = "Urban Bistro & Bakery",
            businessCategory = "Cafe & Bistro",
            wage = "₹800",
            unit = "/ shift (7 hrs)",
            wageAmount = 800.0,
            distanceKm = 2.2,
            time = "Tomorrow, 4:00 PM - 11:00 PM",
            date = "Tomorrow",
            dressCode = "Smart Casual, Neat black polo",
            location = "Jubilee Hills, Hyderabad",
            zone = "Habsiguda",
            instructions = "POS billing and cash handling. Fast math calculations required.",
            workersRequired = 2,
            workersHired = 1,
            category = JobCategory.CASHIER,
            rating = 4.8,
            experienceRequired = "Prior POS experience (Petpooja/Square)",
            isApplied = false,
            isUrgent = false,
            isEmergency30Min = false,
            securityDeposit = 500.0,
            commissionPercent = 6.5
        )
    )

    private fun initialApplicants(): List<JobApplicant> = listOf(
        JobApplicant(
            id = "app_1",
            jobId = "1",
            jobTitle = "Senior Catering Steward",
            workerName = "Ramesh Kumar",
            role = "Senior Steward",
            rating = 4.85,
            jobsCompleted = 47,
            onTimeRate = 98,
            trustScore = 96,
            isVerified = true,
            status = ApplicantStatus.CHECKED_IN,
            checkInTime = "4:52 PM Today",
            appliedAt = "1 hr ago",
            isStandbyReady = true,
            distanceKm = 1.2
        ),
        JobApplicant(
            id = "app_2",
            jobId = "1",
            jobTitle = "Senior Catering Steward",
            workerName = "Suresh Verma",
            role = "Kitchen Hand & Steward",
            rating = 4.70,
            jobsCompleted = 32,
            onTimeRate = 94,
            trustScore = 91,
            isVerified = true,
            status = ApplicantStatus.CHECKED_IN,
            checkInTime = "5:01 PM Today",
            appliedAt = "2 hrs ago",
            isStandbyReady = true,
            distanceKm = 2.1
        ),
        JobApplicant(
            id = "app_3",
            jobId = "1",
            jobTitle = "Senior Catering Steward",
            workerName = "Kiran Patel",
            role = "Banquet Steward",
            rating = 4.40,
            jobsCompleted = 19,
            onTimeRate = 88,
            trustScore = 84,
            isVerified = true,
            status = ApplicantStatus.HIRED,
            checkInTime = null,
            appliedAt = "3 hrs ago",
            isStandbyReady = false,
            distanceKm = 3.5
        ),
        JobApplicant(
            id = "app_4",
            jobId = "2",
            jobTitle = "Kitchen Line Helper & Prep",
            workerName = "Mahesh Biradar",
            role = "Kitchen Assistant",
            rating = 4.25,
            jobsCompleted = 12,
            onTimeRate = 90,
            trustScore = 78,
            isVerified = false,
            status = ApplicantStatus.PENDING,
            appliedAt = "30 mins ago",
            isStandbyReady = true,
            distanceKm = 1.8
        ),
        JobApplicant(
            id = "app_5",
            jobId = "3",
            jobTitle = "VIP Event Host & Usher",
            workerName = "Priya Sharma",
            role = "Guest Relations Usher",
            rating = 4.95,
            jobsCompleted = 58,
            onTimeRate = 100,
            trustScore = 99,
            isVerified = true,
            status = ApplicantStatus.PENDING,
            appliedAt = "10 mins ago",
            isStandbyReady = true,
            distanceKm = 0.9
        )
    )

    private fun initialTransactions(): List<PayoutTransaction> = listOf(
        PayoutTransaction(
            id = "tx1",
            title = "Grand Hotel Banquet Shift",
            businessOrWorker = "Royal Feast Caterers",
            date = "Yesterday",
            grossAmount = 900.0,
            platformFee = 58.5,
            netAmount = 841.5,
            isCredit = true,
            paymentMethod = "Instant UPI",
            status = "Completed (Escrow Released)"
        ),
        PayoutTransaction(
            id = "tx2",
            title = "Urban Cafe Steward Shift",
            businessOrWorker = "Spice Bazaar Restaurant",
            date = "3 days ago",
            grossAmount = 700.0,
            platformFee = 45.5,
            netAmount = 654.5,
            isCredit = true,
            paymentMethod = "Instant UPI",
            status = "Completed (Escrow Released)"
        ),
        PayoutTransaction(
            id = "tx3",
            title = "Weekend Wedding Catering",
            businessOrWorker = "Elite Celebrations",
            date = "Last week",
            grossAmount = 1850.0,
            platformFee = 120.25,
            netAmount = 1729.75,
            isCredit = true,
            paymentMethod = "Instant UPI",
            status = "Completed (Escrow Released)"
        ),
        PayoutTransaction(
            id = "tx4",
            title = "Instant UPI Bank Transfer",
            businessOrWorker = "Google Pay UPI (HDFC Bank)",
            date = "10 days ago",
            grossAmount = 5000.0,
            platformFee = 0.0,
            netAmount = 5000.0,
            isCredit = false,
            paymentMethod = "UPI Auto-Settlement",
            status = "Settled in Bank"
        )
    )

    private fun initialDisputes(): List<DisputeReport> = listOf(
        DisputeReport(
            id = "disp_101",
            reportedByRole = "Worker",
            reporterName = "Ramesh Kumar",
            targetName = "Old Town Bakery (Shift #89)",
            reason = "Payment Dispute (2h Overtime)",
            details = "Worked additional 2 hours beyond scheduled shift due to sudden wedding guest influx. Extra ₹300 pending.",
            evidenceLogs = listOf(
                "GPS Log: Stayed within 100m geofence until 11:30 PM",
                "Supervisor WhatsApp Chat Confirmation: 'Please stay 2 more hours, will add in payout'",
                "Store QR checkout logged at 11:34 PM"
            ),
            status = "Resolved - Payment Released",
            timestamp = "3 days ago",
            adminRuling = "Verified via GPS overtime records. ₹300 released to worker wallet."
        ),
        DisputeReport(
            id = "disp_102",
            reportedByRole = "Owner",
            reporterName = "Spice Bazaar Restaurant",
            targetName = "Vikram S. (Candidate)",
            reason = "Worker No-Show without Notice",
            details = "Hired worker did not arrive for evening dinner rush and did not answer phone calls.",
            evidenceLogs = listOf(
                "No GPS arrival signal received within 15 mins of shift start",
                "3 call logs unacknowledged",
                "Replacement worker dispatched from Standby queue"
            ),
            status = "Under Review",
            timestamp = "Today",
            adminRuling = null
        )
    )

    private fun initialNotifications(): List<NotificationItem> = listOf(
        NotificationItem("n1", "⚡ Urgent 30-Min Shift Nearby", "Royal Feast Caterers needs 2 Standby Stewards within 1.2km.", "5m ago", "EMERGENCY", false),
        NotificationItem("n2", "Attendance Streak Maintained", "You achieved 98% on-time check-in! Platinum badge unlocked.", "2h ago", "ALERT", false),
        NotificationItem("n3", "Weekly Escrow Payout Credited", "₹12,450 successfully settled for completed shifts this month.", "1d ago", "PAYOUT", true)
    )
}
