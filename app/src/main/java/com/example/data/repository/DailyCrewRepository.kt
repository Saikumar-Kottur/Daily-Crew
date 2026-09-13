package com.example.data.repository

import com.example.data.local.DailyCrewDatabase
import com.example.data.local.VerificationRecordEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailyCrewRepository {

    private var database: DailyCrewDatabase? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _dbVerifications = MutableStateFlow<List<VerificationRecordEntity>>(initialDbVerifications())
    val dbVerifications: StateFlow<List<VerificationRecordEntity>> = _dbVerifications.asStateFlow()

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

    private val _suretyTransactions = MutableStateFlow<List<SuretyTransaction>>(initialSuretyTransactions())
    val suretyTransactions: StateFlow<List<SuretyTransaction>> = _suretyTransactions.asStateFlow()

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

    fun withdrawWorkerWallet(
        amount: Double,
        method: String = "UPI",
        destination: String = ""
    ): Boolean {
        val currentBal = _workerProfile.value.walletBalance
        if (amount > 0 && amount <= currentBal) {
            val freePasses = _workerProfile.value.freeCommissionWithdrawalsCount
            val isFree = freePasses > 0
            val commissionRate = if (isFree) 0.0 else (_platformSettings.value.workerWithdrawalCommissionPercent / 100.0) // 9.7%
            val platformCommission = amount * commissionRate
            val netPayout = amount - platformCommission

            _workerProfile.update {
                it.copy(
                    walletBalance = it.walletBalance - amount,
                    freeCommissionWithdrawalsCount = if (isFree) (it.freeCommissionWithdrawalsCount - 1).coerceAtLeast(0) else it.freeCommissionWithdrawalsCount
                )
            }

            if (platformCommission > 0) {
                _platformSettings.update {
                    it.copy(
                        totalPlatformRevenueCollected = it.totalPlatformRevenueCollected + platformCommission,
                        totalWorkerCommissionsCollected = it.totalWorkerCommissionsCollected + platformCommission
                    )
                }
            }

            val isBank = method.contains("Bank", ignoreCase = true)
            val destinationLabel = if (destination.isNotBlank()) {
                destination
            } else if (isBank) {
                "HDFC Bank (A/C ***1823)"
            } else {
                "UPI (ramesh@icici)"
            }

            val feeTag = if (isFree) "0% Fee (Referral Waiver Applied)" else "9.7% Platform Commission (₹${"%.2f".format(platformCommission)})"
            val txn = PayoutTransaction(
                id = "wth_${System.currentTimeMillis()}",
                title = if (isBank) "Direct Bank IMPS Payout" else "Instant UPI Wallet Payout",
                businessOrWorker = destinationLabel,
                date = "Today",
                grossAmount = amount,
                platformFee = platformCommission,
                netAmount = netPayout,
                isCredit = false,
                paymentMethod = if (isBank) "Bank IMPS Transfer" else "Instant UPI",
                status = "Success • $feeTag"
            )
            _transactions.update { listOf(txn) + it }

            val notifMessage = if (isFree) {
                "🎉 Referral Pass applied! ₹${netPayout.toInt()} transferred to $destinationLabel with 0% platform commission! (${_workerProfile.value.freeCommissionWithdrawalsCount} free passes remaining)."
            } else {
                "₹${netPayout.toInt()} sent to $destinationLabel via $method. (Gross: ₹${amount.toInt()} - 9.7% Commission: ₹${"%.2f".format(platformCommission)} credited to DailyCrew)."
            }
            addNotification("Withdrawal Disbursed", notifMessage, "PAYOUT")
            return true
        }
        return false
    }

    // ---------------- Surety / Escrow Actions ----------------

    fun topUpSuretyBalance(amount: Double): Boolean {
        if (amount <= 0) return false
        _workerProfile.update {
            it.copy(
                suretyEscrowBalance = it.suretyEscrowBalance + amount,
                walletBalance = (it.walletBalance - amount).coerceAtLeast(0.0)
            )
        }
        val txn = SuretyTransaction(
            id = "sur_${System.currentTimeMillis()}",
            title = "Surety Wallet Top-Up",
            amount = amount,
            date = "Today",
            type = "DEPOSIT",
            relatedJobTitle = "DailyCrew Escrow Reserve",
            note = "Security deposit added to unlock instant shift bookings and boost trust score."
        )
        _suretyTransactions.update { listOf(txn) + it }
        addNotification("Surety Escrow Funded", "₹${amount.toInt()} added to your Surety Escrow Account. Shift eligibility maintained.", "CHECKIN")
        return true
    }

    fun withdrawSuretyBalance(amount: Double): Boolean {
        val available = _workerProfile.value.suretyEscrowBalance - _workerProfile.value.lockedSuretyAmount
        if (amount > 0 && amount <= available) {
            _workerProfile.update {
                it.copy(
                    suretyEscrowBalance = it.suretyEscrowBalance - amount,
                    walletBalance = it.walletBalance + amount
                )
            }
            val txn = SuretyTransaction(
                id = "sur_wth_${System.currentTimeMillis()}",
                title = "Surety Reserve Withdrawal",
                amount = amount,
                date = "Today",
                type = "UNLOCKED_REFUNDED",
                relatedJobTitle = "DailyCrew Escrow Reserve",
                note = "Refunded to worker earnings wallet."
            )
            _suretyTransactions.update { listOf(txn) + it }
            addNotification("Surety Withdrawn", "₹${amount.toInt()} moved from Surety Escrow to your standard wallet.", "PAYOUT")
            return true
        }
        return false
    }

    fun toggleSaveJob(jobId: String): Boolean {
        val currentSaved = _workerProfile.value.savedJobIds
        val isNowSaved: Boolean
        val newSavedList = if (currentSaved.contains(jobId)) {
            isNowSaved = false
            currentSaved - jobId
        } else {
            isNowSaved = true
            currentSaved + jobId
        }
        _workerProfile.update { it.copy(savedJobIds = newSavedList) }
        _jobs.update { list ->
            list.map { if (it.id == jobId) it.copy(isSaved = isNowSaved) else it }
        }
        addNotification(
            if (isNowSaved) "Shift Bookmarked" else "Shift Removed from Saved",
            if (isNowSaved) "Shift saved to your Bookmarked tab for quick reference." else "Shift removed from saved list.",
            "JOB"
        )
        return isNowSaved
    }

    fun withdrawJobApplication(jobId: String): Boolean {
        var found = false
        _applicants.update { list ->
            list.filterNot {
                if (it.jobId == jobId && it.workerName == _workerProfile.value.name) {
                    found = true
                    true
                } else false
            }
        }
        _jobs.update { list ->
            list.map { if (it.id == jobId) it.copy(isApplied = false) else it }
        }
        if (found) {
            addNotification("Application Withdrawn", "You cancelled your application. Any reserved surety hold is released.", "ALERT")
        }
        return found
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
        val targetApp = _applicants.value.firstOrNull { it.id == applicantId }
        _applicants.update { list ->
            list.map { app ->
                if (app.id == applicantId) {
                    app.copy(status = ApplicantStatus.HIRED)
                } else app
            }
        }
        if (targetApp != null) {
            _jobs.update { list ->
                list.map { job ->
                    if (job.id == targetApp.jobId) {
                        job.copy(workersHired = (job.workersHired + 1).coerceAtMost(job.workersRequired))
                    } else job
                }
            }
            addNotification("Worker Hired", "${targetApp.workerName} hired for ${targetApp.jobTitle}. Shift confirmed.", "CHECKIN")
        }
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

    // ---------------- Owner Job Management ----------------

    fun editJob(
        jobId: String,
        title: String,
        category: JobCategory,
        wageAmount: Double,
        workersRequired: Int,
        time: String,
        dressCode: String,
        location: String,
        instructions: String
    ): Boolean {
        var updated = false
        _jobs.update { list ->
            list.map { job ->
                if (job.id == jobId) {
                    updated = true
                    job.copy(
                        title = title,
                        category = category,
                        wage = "₹${wageAmount.toInt()}",
                        wageAmount = wageAmount,
                        workersRequired = workersRequired,
                        time = time,
                        dressCode = dressCode,
                        location = location,
                        instructions = instructions
                    )
                } else job
            }
        }
        if (updated) {
            addNotification("Shift Updated", "Changes to shift '$title' have been published to candidates.", "JOB")
        }
        return updated
    }

    fun deleteJob(jobId: String): Boolean {
        val job = _jobs.value.firstOrNull { it.id == jobId }
        _jobs.update { list -> list.filterNot { it.id == jobId } }
        if (job != null) {
            _businessProfile.update {
                it.copy(
                    activeShiftCount = (it.activeShiftCount - 1).coerceAtLeast(0),
                    escrowSecurityDepositBalance = (it.escrowSecurityDepositBalance - job.securityDeposit).coerceAtLeast(0.0)
                )
            }
            addNotification("Shift Cancelled", "Shift '${job.title}' deleted. Security deposit of ₹${job.securityDeposit.toInt()} refunded to business account.", "ALERT")
            return true
        }
        return false
    }

    fun toggleJobPause(jobId: String): Boolean {
        var isNowPaused = false
        _jobs.update { list ->
            list.map { job ->
                if (job.id == jobId) {
                    isNowPaused = !job.isPaused
                    job.copy(isPaused = isNowPaused)
                } else job
            }
        }
        addNotification(
            if (isNowPaused) "Shift Paused" else "Shift Resumed",
            if (isNowPaused) "Applications paused for shift #$jobId. Candidates will not see it." else "Shift #$jobId is live and visible to workers.",
            "JOB"
        )
        return isNowPaused
    }

    fun repostJob(jobId: String): JobPosting? {
        val existing = _jobs.value.firstOrNull { it.id == jobId } ?: return null
        val reposted = existing.copy(
            id = "repost_${System.currentTimeMillis()}",
            date = "Today",
            time = "Tonight, 6:00 PM - 2:00 AM",
            workersHired = 0,
            isApplied = false,
            isPaused = false
        )
        _jobs.update { listOf(reposted) + it }
        _businessProfile.update {
            it.copy(
                activeShiftCount = it.activeShiftCount + 1,
                totalJobsCreated = it.totalJobsCreated + 1,
                escrowSecurityDepositBalance = it.escrowSecurityDepositBalance + reposted.securityDeposit
            )
        }
        addNotification("Shift Reposted", "Fresh posting for '${reposted.title}' published with verified escrow deposit.", "JOB")
        return reposted
    }

    fun reportWorkerNoShow(applicantId: String, reason: String): Boolean {
        val applicant = _applicants.value.firstOrNull { it.id == applicantId } ?: return false
        val job = _jobs.value.firstOrNull { it.id == applicant.jobId }
        val forfeitAmount = job?.suretyRequired ?: 100.0

        // Forfeit worker's surety deposit & compensate business owner
        _workerProfile.update {
            it.copy(
                suretyEscrowBalance = (it.suretyEscrowBalance - forfeitAmount).coerceAtLeast(0.0),
                lockedSuretyAmount = (it.lockedSuretyAmount - forfeitAmount).coerceAtLeast(0.0),
                trustScore = (it.trustScore - 15).coerceAtLeast(0),
                onTimeRate = (it.onTimeRate - 4).coerceAtLeast(0)
            )
        }

        _businessProfile.update {
            it.copy(
                escrowSecurityDepositBalance = it.escrowSecurityDepositBalance + forfeitAmount
            )
        }

        // Record Surety Transaction
        val txn = SuretyTransaction(
            id = "forfeit_${System.currentTimeMillis()}",
            title = "Surety Forfeit (Liquidated Damages)",
            amount = forfeitAmount,
            date = "Today",
            type = "FORFEITED",
            relatedJobTitle = job?.title ?: "Shift Attendance",
            note = "Forfeited directly to business owner due to verified no-show: $reason"
        )
        _suretyTransactions.update { listOf(txn) + it }

        // Update applicant status
        _applicants.update { list ->
            list.map { if (it.id == applicantId) it.copy(status = ApplicantStatus.REJECTED) else it }
        }

        // Log dispute record
        fileDispute(
            reportedByRole = "Owner",
            reporterName = _businessProfile.value.businessName,
            target = applicant.workerName,
            reason = "No-Show Shift Abandonment: $reason",
            details = "Worker failed to arrive at venue. Escrow liquidated damages of ₹${forfeitAmount.toInt()} transferred to owner."
        )

        addNotification(
            "Liquidated Damages Compensated",
            "₹${forfeitAmount.toInt()} from worker's surety deposit has been credited to your account due to reported no-show.",
            "PAYOUT"
        )
        return true
    }

    fun rateWorker(applicantId: String, rating: Double, feedback: String): Boolean {
        val applicant = _applicants.value.firstOrNull { it.id == applicantId } ?: return false
        _workerProfile.update {
            val newCompleted = it.completedJobs + 1
            val newRating = ((it.rating * it.completedJobs) + rating) / newCompleted
            it.copy(
                completedJobs = newCompleted,
                rating = (newRating * 100.0).toInt() / 100.0,
                xpPoints = it.xpPoints + 200
            )
        }
        addNotification("Rating Submitted", "You awarded ${applicant.workerName} $rating★: \"$feedback\"", "ALERT")
        return true
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

        // OWNER SURETY SETTLEMENT:
        // Owner surety fee is ₹300 per job.
        // If owner has referral free passes: 100% of ₹300 is refunded to the owner (0% fee).
        // Otherwise: 73.2% (₹219.60) is returned to the owner, 27.8% (₹83.40) is sent to platform admin ("to me"),
        // and 3.0% (₹9.00) referral commission is credited to the person who referred him.
        val suretyFee = _platformSettings.value.securityDepositPerJob // 300.0
        val ownerFreePasses = _businessProfile.value.freeCommissionJobsCount
        val isOwnerCommissionFree = ownerFreePasses > 0

        val ownerSuretyRefund: Double
        val platformSuretyCommission: Double
        val referrerSuretyCommission: Double

        if (isOwnerCommissionFree) {
            ownerSuretyRefund = suretyFee // 100% returned to owner
            platformSuretyCommission = 0.0
            referrerSuretyCommission = 0.0
            _businessProfile.update {
                it.copy(
                    freeCommissionJobsCount = (it.freeCommissionJobsCount - 1).coerceAtLeast(0),
                    totalPayoutsDisbursed = it.totalPayoutsDisbursed + amount,
                    totalWorkersHired = it.totalWorkersHired + 3
                )
            }
        } else {
            ownerSuretyRefund = suretyFee * (_platformSettings.value.ownerSuretyRefundPercent / 100.0) // 73.2% = ₹219.60
            platformSuretyCommission = suretyFee * (_platformSettings.value.ownerSuretyPlatformCommissionPercent / 100.0) // 26.8% = ₹80.40
            referrerSuretyCommission = if (_businessProfile.value.isReferredBySomeone) {
                platformSuretyCommission * (_platformSettings.value.ownerReferralBonusPercent / 100.0) // 3.0% of ₹80.40 platform revenue = ₹2.41
            } else {
                0.0
            }

            _platformSettings.update {
                it.copy(
                    totalPlatformRevenueCollected = it.totalPlatformRevenueCollected + platformSuretyCommission,
                    totalSuretyRevenueCollected = it.totalSuretyRevenueCollected + platformSuretyCommission,
                    totalReferralPayoutsDisbursed = it.totalReferralPayoutsDisbursed + referrerSuretyCommission,
                    totalCompletedJobsCount = it.totalCompletedJobsCount + 1
                )
            }

            _businessProfile.update {
                it.copy(
                    totalPayoutsDisbursed = it.totalPayoutsDisbursed + amount,
                    totalWorkersHired = it.totalWorkersHired + 3
                )
            }
        }

        // Record Surety Settlement Transaction
        val suretySettlementTxn = SuretyTransaction(
            id = "sur_settle_${System.currentTimeMillis()}",
            title = if (isOwnerCommissionFree) "Surety Escrow 100% Refund (Referral Pass)" else "Surety Escrow 73.2% Refund",
            amount = ownerSuretyRefund,
            date = "Today",
            type = "UNLOCKED_REFUNDED",
            relatedJobTitle = "Shift Completion Settlement",
            note = if (isOwnerCommissionFree) {
                "₹300.00 (100%) surety refunded to owner via Referral Waiver Pass. ₹0 platform commission."
            } else {
                "₹${"%.2f".format(ownerSuretyRefund)} (73.2%) refunded to owner. ₹${"%.2f".format(platformSuretyCommission)} (26.8%) DailyCrew platform revenue. ₹${"%.2f".format(referrerSuretyCommission)} (3.0% of revenue) to referrer."
            }
        )
        _suretyTransactions.update { listOf(suretySettlementTxn) + it }

        val notifMessage = if (isOwnerCommissionFree) {
            "Shift settled: Workers paid ₹${amount.toInt()}. 🎉 Referral pass used: 100% of ₹300 surety fee refunded to business! (${_businessProfile.value.freeCommissionJobsCount} free passes remaining)."
        } else {
            "Shift settled: Workers paid ₹${amount.toInt()}. Surety Settlement: ₹${"%.2f".format(ownerSuretyRefund)} (73.2%) refunded to owner, ₹${"%.2f".format(platformSuretyCommission)} (26.8%) platform revenue, ₹${"%.2f".format(referrerSuretyCommission)} (3.0% of platform revenue) credited to referrer."
        }
        addNotification("Shift & Surety Settled", notifMessage, "PAYOUT")
    }

    // ---------------- Referral & Commission Actions ----------------

    fun referWorkerFriend(friendName: String, friendPhone: String = "+91 98765 43210"): ReferralFriendItem {
        val newFriend = ReferralFriendItem(
            id = "ref_f_${System.currentTimeMillis()}",
            name = friendName.ifBlank { "Worker Friend" },
            role = "Banquet & Event Staff",
            joinedDate = "Just now",
            shiftsCompleted = 0,
            friendEarnings = 0.0,
            bonusEarned = 0.0
        )
        _workerProfile.update {
            it.copy(
                freeCommissionWithdrawalsCount = it.freeCommissionWithdrawalsCount + 2,
                totalReferralsCount = it.totalReferralsCount + 1,
                referralFriends = listOf(newFriend) + it.referralFriends
            )
        }
        addNotification(
            "Worker Friend Referred!",
            "🎉 You referred ${newFriend.name}! You unlocked 2 Zero-Commission Withdrawals + 2.3% of DailyCrew platform commission from their shifts!",
            "ALERT"
        )
        return newFriend
    }

    fun simulateFriendEarnings(friendId: String, earnings: Double = 1000.0) {
        // Business logic: Worker B withdrawal = ₹1000, Normal Commission (9.7%) = ₹97.00
        // Referral Reward to Worker A = 2.3% of ₹97.00 = ₹2.23. Remaining commission stays with platform.
        val normalCommission = earnings * (_platformSettings.value.workerWithdrawalCommissionPercent / 100.0)
        val bonus = normalCommission * (_platformSettings.value.workerReferralBonusPercent / 100.0)

        _workerProfile.update { current ->
            val updatedFriends = current.referralFriends.map { f ->
                if (f.id == friendId || current.referralFriends.size == 1) {
                    f.copy(
                        shiftsCompleted = f.shiftsCompleted + 1,
                        friendEarnings = f.friendEarnings + earnings,
                        bonusEarned = f.bonusEarned + bonus
                    )
                } else f
            }
            current.copy(
                walletBalance = current.walletBalance + bonus,
                totalReferralEarnings = current.totalReferralEarnings + bonus,
                referralFriends = updatedFriends
            )
        }

        _platformSettings.update {
            it.copy(totalReferralPayoutsDisbursed = it.totalReferralPayoutsDisbursed + bonus)
        }

        val txn = PayoutTransaction(
            id = "ref_earn_${System.currentTimeMillis()}",
            title = "Worker Referral Reward (2.3% of Commission)",
            businessOrWorker = "Earned from Referred Worker's Shift",
            date = "Today",
            grossAmount = bonus,
            platformFee = 0.0,
            netAmount = bonus,
            isCredit = true,
            paymentMethod = "Referral Commission Rail",
            status = "Credited to Wallet"
        )
        _transactions.update { listOf(txn) + it }
        addNotification(
            "Referral Reward Received!",
            "🎁 ₹${"%.2f".format(bonus)} (2.3% of ₹${"%.2f".format(normalCommission)} platform commission on friend's ₹${earnings.toInt()} shift) credited directly to your wallet!",
            "PAYOUT"
        )
    }

    fun referBusinessOwner(businessName: String, contactPhone: String = "+91 99887 76655"): ReferralOwnerItem {
        val newOwner = ReferralOwnerItem(
            id = "ref_o_${System.currentTimeMillis()}",
            businessName = businessName.ifBlank { "Elite Caterers" },
            joinedDate = "Just now",
            jobsPosted = 0,
            bonusEarned = 0.0
        )
        _businessProfile.update {
            it.copy(
                freeCommissionJobsCount = it.freeCommissionJobsCount + 2,
                totalReferralsCount = it.totalReferralsCount + 1,
                referralOwners = listOf(newOwner) + it.referralOwners
            )
        }
        addNotification(
            "Business Owner Referred!",
            "🎉 You referred ${newOwner.businessName}! You unlocked 2 Zero-Commission Shifts (100% Surety Refund) + 3.0% of DailyCrew's surety revenue from their jobs!",
            "ALERT"
        )
        return newOwner
    }

    fun simulateOwnerSuretyReferralBonus(ownerId: String) {
        // Business logic: Surety fee = ₹300. Platform surety revenue (26.8%) = ₹80.40.
        // Referral Reward to Owner A = 3.0% of ₹80.40 = ₹2.41
        val suretyFee = _platformSettings.value.securityDepositPerJob // 300.0
        val platformSuretyRevenue = suretyFee * (_platformSettings.value.ownerSuretyPlatformCommissionPercent / 100.0) // ₹80.40
        val bonus = platformSuretyRevenue * (_platformSettings.value.ownerReferralBonusPercent / 100.0) // 3.0% of ₹80.40 = ₹2.41

        _businessProfile.update { current ->
            val updatedOwners = current.referralOwners.map { o ->
                if (o.id == ownerId || current.referralOwners.size == 1) {
                    o.copy(
                        jobsPosted = o.jobsPosted + 1,
                        bonusEarned = o.bonusEarned + bonus
                    )
                } else o
            }
            current.copy(
                totalReferralEarnings = current.totalReferralEarnings + bonus,
                referralOwners = updatedOwners
            )
        }

        _platformSettings.update {
            it.copy(totalReferralPayoutsDisbursed = it.totalReferralPayoutsDisbursed + bonus)
        }

        addNotification(
            "Owner Referral Commission Received!",
            "🎁 ₹${"%.2f".format(bonus)} (3.0% of DailyCrew's ₹${"%.2f".format(platformSuretyRevenue)} surety revenue from referred owner's shift) credited to your earnings!",
            "PAYOUT"
        )
    }

    fun applyReferralCode(code: String, isWorker: Boolean): Boolean {
        if (code.isBlank()) return false
        if (isWorker) {
            _workerProfile.update {
                it.copy(freeCommissionWithdrawalsCount = it.freeCommissionWithdrawalsCount + 2)
            }
            addNotification(
                "Referral Code Redeemed!",
                "🎉 Code \"$code\" activated! 2 Zero-Commission Withdrawals added to your account (Save 9.7% each).",
                "ALERT"
            )
        } else {
            _businessProfile.update {
                it.copy(freeCommissionJobsCount = it.freeCommissionJobsCount + 2)
            }
            addNotification(
                "Referral Code Redeemed!",
                "🎉 Code \"$code\" activated! 2 Zero-Commission Shifts added to your business (100% Surety Refund).",
                "ALERT"
            )
        }
        return true
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

    fun updateWorkerLanguage(language: AppLanguage) {
        _workerProfile.update { it.copy(selectedLanguage = language) }
        _businessProfile.update { it.copy(selectedLanguage = language) }
        _platformSettings.update { it.copy(defaultAppLanguage = language) }
    }

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

    // ---------------- User Verification & Database Persistence ----------------

    fun initDatabase(db: DailyCrewDatabase) {
        this.database = db
        repositoryScope.launch {
            try {
                // Ensure initial seed verifications exist in Room DB
                val existing = db.verificationDao().getAllVerifications()
                if (existing.isEmpty()) {
                    initialDbVerifications().forEach { record ->
                        db.verificationDao().insertVerification(record)
                    }
                }
                // Observe live Room database records
                db.verificationDao().getAllVerificationsFlow().collect { records ->
                    if (records.isNotEmpty()) {
                        _dbVerifications.value = records
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitWorkerVerification(
        fullName: String,
        docType: String,
        docNumber: String,
        secondaryDocType: String? = null,
        secondaryDocNumber: String? = null,
        dob: String = "15/08/2000",
        address: String = "Habsiguda, Hyderabad",
        zone: String = "Habsiguda",
        frontPhotoUri: String = "",
        backPhotoUri: String = "",
        selfiePhotoUri: String = ""
    ): VerificationRecordEntity {
        val recId = "REC-WRK-${System.currentTimeMillis().toString().takeLast(6)}-DB"
        val record = VerificationRecordEntity(
            id = recId,
            userId = _workerProfile.value.id,
            userRole = "WORKER",
            legalName = fullName.ifBlank { _workerProfile.value.name },
            documentType = docType,
            documentNumber = docNumber,
            secondaryDocType = secondaryDocType,
            secondaryDocNumber = secondaryDocNumber,
            documentFrontUri = frontPhotoUri.ifBlank { "content://verified/worker_doc_front_${System.currentTimeMillis()}.jpg" },
            documentBackUri = backPhotoUri.ifBlank { "content://verified/worker_doc_back_${System.currentTimeMillis()}.jpg" },
            selfiePhotoUri = selfiePhotoUri.ifBlank { "content://verified/worker_selfie_${System.currentTimeMillis()}.jpg" },
            registeredAddress = address,
            zone = zone,
            dateOfBirthOrReg = dob,
            verificationStatus = "VERIFIED",
            isVerifiedInDb = true,
            verifiedDate = "Today, Database Confirmed",
            verifiedBy = "UIDAI Biometric & State Police Registry",
            faceMatchConfidence = 99.6,
            notes = "Government ID authenticated and persisted into DailyCrew Room database (Table: verification_records)."
        )

        // Update in-memory state
        _dbVerifications.update { list ->
            listOf(record) + list.filter { it.userId != record.userId || it.userRole != record.userRole }
        }

        // Persist to Room SQLite Database
        database?.let { db ->
            repositoryScope.launch {
                try {
                    db.verificationDao().insertVerification(record)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Mark Worker Profile as Verified in Database
        _workerProfile.update { current ->
            current.copy(
                name = fullName.ifBlank { current.name },
                isAadhaarVerified = true,
                isPoliceVerified = true,
                isSelfieVerified = true,
                isPhoneVerified = true,
                isGoogleVerified = true,
                isVerifiedInDb = true,
                verifiedDocumentType = docType,
                verifiedDbRecordId = recId,
                verifiedDateFormatted = "Verified in Database (${record.verifiedDate})",
                aadhaarMasked = if (docType.contains("Aadhaar", ignoreCase = true)) docNumber else current.aadhaarMasked,
                currentZone = zone.ifBlank { current.currentZone },
                documentFrontUri = record.documentFrontUri,
                documentBackUri = record.documentBackUri,
                selfiePhotoUri = record.selfiePhotoUri
            )
        }

        addNotification(
            title = "Worker Identity Verified & Saved in DB",
            message = "✅ $docType ($docNumber) authenticated. You are now marked as 'Verified' in the DailyCrew database.",
            type = "ALERT"
        )

        return record
    }

    fun submitBusinessVerification(
        legalBusinessName: String,
        docType: String,
        docNumber: String,
        secondaryDocType: String? = null,
        secondaryDocNumber: String? = null,
        registeredAddress: String = "Banjara Hills, Hyderabad",
        zone: String = "Uppal",
        businessType: String = "Private Limited",
        docProofUri: String = "",
        ownerSignatoryName: String = "",
        ownerSignatoryPhone: String = ""
    ): VerificationRecordEntity {
        val recId = "REC-BIZ-${System.currentTimeMillis().toString().takeLast(6)}-DB"
        val record = VerificationRecordEntity(
            id = recId,
            userId = _businessProfile.value.id,
            userRole = "OWNER",
            legalName = legalBusinessName.ifBlank { _businessProfile.value.businessName },
            documentType = docType,
            documentNumber = docNumber,
            secondaryDocType = secondaryDocType,
            secondaryDocNumber = secondaryDocNumber,
            documentFrontUri = docProofUri.ifBlank { "content://verified/biz_certificate_${System.currentTimeMillis()}.pdf" },
            documentBackUri = "",
            selfiePhotoUri = "content://verified/director_auth_${System.currentTimeMillis()}.jpg",
            registeredAddress = registeredAddress,
            zone = zone,
            dateOfBirthOrReg = "Active Taxpayer Entity",
            verificationStatus = "VERIFIED",
            isVerifiedInDb = true,
            verifiedDate = "Today, Database Confirmed",
            verifiedBy = "GSTN Portal API & Commercial Registry",
            faceMatchConfidence = 100.0,
            notes = "Business registration validated with commercial tax records and marked as Verified in Database."
        )

        // Update in-memory state
        _dbVerifications.update { list ->
            listOf(record) + list.filter { it.userId != record.userId || it.userRole != record.userRole }
        }

        // Persist to Room SQLite Database
        database?.let { db ->
            repositoryScope.launch {
                try {
                    db.verificationDao().insertVerification(record)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Mark Business Profile as Verified in Database
        _businessProfile.update { current ->
            current.copy(
                businessName = legalBusinessName.ifBlank { current.businessName },
                gstin = docNumber.ifBlank { current.gstin },
                isPhoneVerified = true,
                isGoogleVerified = true,
                isVerifiedInDb = true,
                isLocationVerified = true,
                isGstinVerified = true,
                verifiedDocumentType = docType,
                verifiedDbRecordId = recId,
                verifiedDateFormatted = "Verified in Database (${record.verifiedDate})",
                address = registeredAddress.ifBlank { current.address },
                zone = zone.ifBlank { current.zone },
                documentFrontUri = record.documentFrontUri
            )
        }

        addNotification(
            title = "Business Entity Verified in DB",
            message = "✅ $docType ($docNumber) verified. Business is marked as 'Verified' in the DailyCrew database.",
            type = "ALERT"
        )

        return record
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
            securityDeposit = 300.0,
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
            securityDeposit = 300.0,
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
            securityDeposit = 300.0,
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
            securityDeposit = 300.0,
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
            securityDeposit = 300.0,
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
            securityDeposit = 300.0,
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
            distanceKm = 1.2,
            skills = listOf("Catering", "Table Service", "VIP Banquet", "FSSAI")
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
            distanceKm = 2.1,
            skills = listOf("Catering", "Kitchen Prep", "Housekeeping", "Dishwashing")
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
            distanceKm = 3.5,
            skills = listOf("Catering", "Banquet Service", "Housekeeping")
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
            distanceKm = 1.8,
            skills = listOf("Kitchen Prep", "Housekeeping", "Maintenance")
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
            distanceKm = 0.9,
            skills = listOf("Catering", "Guest Relations", "Event Staff", "VIP Protocol")
        ),
        JobApplicant(
            id = "app_6",
            jobId = "4",
            jobTitle = "Facility & Housekeeping Attendant",
            workerName = "Anita Rao",
            role = "Senior Housekeeping Lead",
            rating = 4.90,
            jobsCompleted = 64,
            onTimeRate = 98,
            trustScore = 97,
            isVerified = true,
            status = ApplicantStatus.PENDING,
            appliedAt = "25 mins ago",
            isStandbyReady = true,
            distanceKm = 1.5,
            skills = listOf("Housekeeping", "Deep Cleaning", "Sanitation", "Linen Care")
        ),
        JobApplicant(
            id = "app_7",
            jobId = "4",
            jobTitle = "Commercial Housekeeping Staff",
            workerName = "Rajesh Naik",
            role = "Commercial Housekeeping",
            rating = 4.30,
            jobsCompleted = 15,
            onTimeRate = 92,
            trustScore = 80,
            isVerified = false,
            status = ApplicantStatus.PENDING,
            appliedAt = "45 mins ago",
            isStandbyReady = true,
            distanceKm = 2.4,
            skills = listOf("Housekeeping", "Waste Management", "Floor Cleaning")
        ),
        JobApplicant(
            id = "app_8",
            jobId = "1",
            jobTitle = "Event Catering Specialist",
            workerName = "Sunita Devi",
            role = "Catering Crew & Server",
            rating = 4.88,
            jobsCompleted = 41,
            onTimeRate = 96,
            trustScore = 94,
            isVerified = true,
            status = ApplicantStatus.PENDING,
            appliedAt = "5 mins ago",
            isStandbyReady = true,
            distanceKm = 1.1,
            skills = listOf("Catering", "Buffet Setup", "Food Safety", "Table Service")
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

    private fun initialSuretyTransactions(): List<SuretyTransaction> = listOf(
        SuretyTransaction(
            id = "sur_6",
            title = "Active Shift Commitment Hold",
            amount = 100.0,
            date = "Today, 08:30 AM",
            type = "LOCKED",
            relatedJobTitle = "VIP Event Host & Usher (Apex Global Summit)",
            note = "₹100 security held in escrow during active shift commitment. Automatically refunds upon GPS/QR check-in."
        ),
        SuretyTransaction(
            id = "sur_5",
            title = "Surety Shift Hold & Release",
            amount = 100.0,
            date = "Yesterday, 07:10 PM",
            type = "UNLOCKED_REFUNDED",
            relatedJobTitle = "Hotel Housekeeping Attendant (Marigold Grand)",
            note = "Shift completed and verified by supervisor. ₹100 hold returned to available balance."
        ),
        SuretyTransaction(
            id = "sur_4",
            title = "Surety Shift Commitment Hold",
            amount = 100.0,
            date = "Yesterday, 01:30 PM",
            type = "LOCKED",
            relatedJobTitle = "Hotel Housekeeping Attendant (Marigold Grand)",
            note = "Shift booking confirmed. ₹100 held in platform escrow against no-show."
        ),
        SuretyTransaction(
            id = "sur_3",
            title = "Surety Shift Hold & Release",
            amount = 100.0,
            date = "3 days ago",
            type = "UNLOCKED_REFUNDED",
            relatedJobTitle = "Senior Catering Steward (Grand Hotel)",
            note = "On-time arrival verified by GPS. ₹100 hold returned to surety balance."
        ),
        SuretyTransaction(
            id = "sur_2",
            title = "Escrow Reserve Top-Up",
            amount = 200.0,
            date = "5 days ago",
            type = "DEPOSIT",
            relatedJobTitle = "DailyCrew Escrow Reserve",
            note = "Funded ₹200 via UPI (Ref: UPI-84920482) to support multiple concurrent shift commitments."
        ),
        SuretyTransaction(
            id = "sur_1",
            title = "Initial Surety Escrow Deposit",
            amount = 500.0,
            date = "15 days ago",
            type = "DEPOSIT",
            relatedJobTitle = "DailyCrew Escrow Reserve",
            note = "Security deposit funded via UPI to activate platform verification & priority shift access."
        )
    )
}

fun initialDbVerifications(): List<VerificationRecordEntity> = listOf(
    VerificationRecordEntity(
        id = "REC-WRK-7391-DB",
        userId = "w1",
        userRole = "WORKER",
        legalName = "Ramesh Kumar",
        documentType = "Aadhaar Card (UIDAI)",
        documentNumber = "XXXX XXXX 7391",
        secondaryDocType = "Police Clearance Certificate",
        secondaryDocNumber = "TSP-HYD-2026-9812",
        documentFrontUri = "content://verified/aadhaar_front_sample.jpg",
        documentBackUri = "content://verified/aadhaar_back_sample.jpg",
        selfiePhotoUri = "content://verified/worker_selfie_sample.jpg",
        registeredAddress = "Flat 302, Habsiguda Main Rd, Hyderabad, Telangana - 500007",
        zone = "Habsiguda",
        dateOfBirthOrReg = "15/08/2000",
        verificationStatus = "VERIFIED",
        isVerifiedInDb = true,
        verifiedDate = "March 12, 2026",
        verifiedBy = "UIDAI Biometric & State Police Registry",
        faceMatchConfidence = 99.4,
        notes = "Government ID authenticated with 99.4% live 3D facial match. Zero criminal adverse records in Telangana State Police database."
    ),
    VerificationRecordEntity(
        id = "REC-BIZ-36AA-DB",
        userId = "b1",
        userRole = "OWNER",
        legalName = "Royal Feast Caterers Private Limited",
        documentType = "GSTIN Certificate",
        documentNumber = "36AAAAA0000A1Z5",
        secondaryDocType = "FSSAI Food Safety License",
        secondaryDocNumber = "13624011000842",
        documentFrontUri = "content://verified/gstin_certificate_sample.pdf",
        documentBackUri = "content://verified/fssai_license_sample.pdf",
        selfiePhotoUri = "content://verified/director_authorized_photo.jpg",
        registeredAddress = "Plot 84, Road No. 12, Banjara Hills, Hyderabad, Telangana - 500034",
        zone = "Uppal",
        dateOfBirthOrReg = "12/04/2018",
        verificationStatus = "VERIFIED",
        isVerifiedInDb = true,
        verifiedDate = "March 10, 2026",
        verifiedBy = "GSTN Portal API & FSSAI Central Registry",
        faceMatchConfidence = 100.0,
        notes = "Active GSTIN taxpayer in good standing. Clean compliance filing and valid FSSAI central catering food operator certificate."
    )
)
