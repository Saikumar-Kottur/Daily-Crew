package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyCrewDatabase
import com.example.data.local.VerificationRecordEntity
import com.example.data.model.*
import com.example.data.repository.DailyCrewRepository
import com.example.notification.DailyCrewNotificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DailyCrewUiState(
    val isAuthenticated: Boolean = false,
    val loggedInUserName: String = "Ravi Kumar",
    val loggedInUserPhone: String = "+91 98765 43210",
    val role: AppRole = AppRole.WORKER,
    val bottomNavIndex: Int = 0,
    val searchQuery: String = "",
    val selectedCategory: JobCategory = JobCategory.ALL,
    val selectedZoneFilter: String = "All", // Pilot zones: Uppal, Habsiguda, Tarnaka, Nacharam
    val filterRadiusKm: Float = 10f,
    val minWageFilter: Float = 0f,
    val filterEmergencyOnly: Boolean = false,
    val filterVerifiedOnly: Boolean = false,
    val workerActiveTab: WorkerJobTab = WorkerJobTab.NEARBY,
    val selectedJobForDetails: JobPosting? = null,
    val isCheckInScannerOpen: Boolean = false,
    val isWithdrawDialogOpen: Boolean = false,
    val isDisputeDialogOpen: Boolean = false,
    val isEmergencyHiringDialogOpen: Boolean = false,
    val isSuretyDialogOpen: Boolean = false,
    val isEditJobDialogOpen: Boolean = false,
    val selectedJobForEdit: JobPosting? = null,
    val isRateWorkerDialogOpen: Boolean = false,
    val selectedApplicantForRating: JobApplicant? = null,
    val isReportNoShowDialogOpen: Boolean = false,
    val selectedApplicantForNoShow: JobApplicant? = null,
    val isChatSheetOpen: Boolean = false,
    val activeChatApplicant: JobApplicant? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val isReferDialogOpen: Boolean = false,
    val isVerificationDialogOpen: Boolean = false,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val snackbarMessage: String? = null
)

data class ChatMessage(
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val isLocationShare: Boolean = false
)

class DailyCrewViewModel(
    private val repository: DailyCrewRepository = DailyCrewRepository()
) : ViewModel() {

    private var notificationService: DailyCrewNotificationService? = null

    fun setNotificationService(service: DailyCrewNotificationService) {
        this.notificationService = service
    }

    private val _uiState = MutableStateFlow(DailyCrewUiState())
    val uiState: StateFlow<DailyCrewUiState> = _uiState.asStateFlow()

    val jobs: StateFlow<List<JobPosting>> = repository.jobs
    val workerProfile: StateFlow<WorkerProfile> = repository.workerProfile
    val businessProfile: StateFlow<BusinessProfile> = repository.businessProfile
    val applicants: StateFlow<List<JobApplicant>> = repository.applicants
    val transactions: StateFlow<List<PayoutTransaction>> = repository.transactions
    val disputes: StateFlow<List<DisputeReport>> = repository.disputes
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val platformSettings: StateFlow<PlatformGlobalSettings> = repository.platformSettings
    val bannedUsers: StateFlow<List<String>> = repository.bannedUsers
    val suretyTransactions: StateFlow<List<SuretyTransaction>> = repository.suretyTransactions
    val dbVerifications: StateFlow<List<VerificationRecordEntity>> = repository.dbVerifications

    // Filtered Jobs based on search query, category, zone, wage, emergency tag
    val filteredJobs: StateFlow<List<JobPosting>> = combine(
        repository.jobs,
        _uiState
    ) { allJobs, state ->
        allJobs.filter { job ->
            val matchesQuery = state.searchQuery.isBlank() ||
                    job.title.contains(state.searchQuery, ignoreCase = true) ||
                    job.businessName.contains(state.searchQuery, ignoreCase = true) ||
                    job.location.contains(state.searchQuery, ignoreCase = true)

            val matchesCategory = state.selectedCategory == JobCategory.ALL ||
                    job.category == state.selectedCategory

            val matchesRadius = job.distanceKm <= state.filterRadiusKm

            val matchesWage = job.wageAmount >= state.minWageFilter

            val matchesZone = state.selectedZoneFilter == "All" ||
                    job.zone.equals(state.selectedZoneFilter, ignoreCase = true)

            val matchesEmergency = !state.filterEmergencyOnly || job.isEmergency30Min || job.isUrgent

            val notPaused = !job.isPaused

            matchesQuery && matchesCategory && matchesRadius && matchesWage && matchesZone && matchesEmergency && notPaused
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Job Matching Recommendations
    val aiRecommendedJobs: StateFlow<List<JobPosting>> = combine(
        repository.jobs,
        repository.workerProfile
    ) { allJobs, worker ->
        allJobs.filter { !it.isPaused }.sortedByDescending { job ->
            var score = 0
            if (worker.skills.any { job.title.contains(it, ignoreCase = true) || job.category.label.contains(it, ignoreCase = true) }) score += 40
            if (job.distanceKm < 2.0) score += 30
            if (job.rating >= 4.7) score += 20
            if (job.isUrgent || job.isEmergency30Min) score += 10
            score
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worker Tab-specific filtered jobs (Nearby, Recommended, Applied, Accepted, Completed, Saved)
    val workerTabFilteredJobs: StateFlow<List<JobPosting>> = combine(
        filteredJobs,
        aiRecommendedJobs,
        repository.applicants,
        repository.workerProfile,
        _uiState
    ) { nearby, aiRecs, appList, worker, state ->
        val allJobs = repository.jobs.value
        val myName = worker.name
        val appliedIds = appList.filter { it.workerName == myName }.map { it.jobId }.toSet()
        val acceptedIds = appList.filter { it.workerName == myName && (it.status == ApplicantStatus.HIRED || it.status == ApplicantStatus.CHECKED_IN) }.map { it.jobId }.toSet()
        val completedIds = appList.filter { it.workerName == myName && it.status == ApplicantStatus.COMPLETED }.map { it.jobId }.toSet()

        when (state.workerActiveTab) {
            WorkerJobTab.NEARBY -> nearby.sortedBy { it.distanceKm }
            WorkerJobTab.RECOMMENDED -> aiRecs
            WorkerJobTab.APPLIED -> allJobs.filter { it.isApplied || appliedIds.contains(it.id) }
            WorkerJobTab.ACCEPTED -> allJobs.filter { acceptedIds.contains(it.id) }
            WorkerJobTab.COMPLETED -> allJobs.filter { completedIds.contains(it.id) || it.isCompleted }
            WorkerJobTab.SAVED -> allJobs.filter { worker.savedJobIds.contains(it.id) || it.isSaved }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Top Verified Applicants for Owner
    val aiTopApplicants: StateFlow<List<JobApplicant>> = repository.applicants.map { list ->
        list.sortedByDescending { it.trustScore * 10 + (it.rating * 10).toInt() + if (it.isVerified) 50 else 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchRole(newRole: AppRole) {
        _uiState.update { it.copy(role = newRole, bottomNavIndex = 0) }
    }

    fun login(role: AppRole, name: String = "User", phone: String = "+91 98765 43210") {
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                role = role,
                bottomNavIndex = 0,
                loggedInUserName = name,
                loggedInUserPhone = phone,
                snackbarMessage = "Signed in as $name"
            )
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                role = AppRole.WORKER,
                bottomNavIndex = 0,
                snackbarMessage = "Signed out successfully"
            )
        }
    }

    fun setBottomNavIndex(index: Int) {
        _uiState.update { it.copy(bottomNavIndex = index) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: JobCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setZoneFilter(zone: String) {
        _uiState.update { it.copy(selectedZoneFilter = zone) }
    }

    fun toggleEmergencyFilter() {
        _uiState.update { it.copy(filterEmergencyOnly = !it.filterEmergencyOnly) }
    }

    fun setRadius(radiusKm: Float) {
        _uiState.update { it.copy(filterRadiusKm = radiusKm) }
    }

    fun setMinWageFilter(wage: Float) {
        _uiState.update { it.copy(minWageFilter = wage) }
    }

    // Worker Standby Toggle ("Available Now")
    fun toggleWorkerStandby() {
        val active = repository.toggleWorkerStandby()
        if (active) {
            showSnackbar("🟢 Standby Mode ON: You're visible to nearby businesses for instant 30-min hire!")
        } else {
            showSnackbar("⚪ Standby Mode OFF: Shift dispatch paused.")
        }
    }

    fun openJobDetails(job: JobPosting) {
        _uiState.update { it.copy(selectedJobForDetails = job) }
    }

    fun closeJobDetails() {
        _uiState.update { it.copy(selectedJobForDetails = null) }
    }

    fun applyForJob(jobId: String) {
        val applicant = repository.applyForJob(jobId)
        if (applicant != null) {
            showSnackbar("Application sent! Owner notified instantly.")
            notificationService?.notifyNewApplicantAlert(
                applicant = applicant,
                jobTitle = applicant.jobTitle
            )
            _uiState.update { state ->
                val updatedJob = state.selectedJobForDetails?.let {
                    if (it.id == jobId) it.copy(isApplied = true) else it
                }
                state.copy(selectedJobForDetails = updatedJob)
            }
        }
    }

    fun postJob(
        title: String,
        category: JobCategory,
        wageAmount: Double,
        workersCount: Int,
        dressCode: String,
        instructions: String,
        location: String,
        time: String,
        zone: String = "Uppal"
    ) {
        val newJob = repository.postNewJob(
            title = title,
            category = category,
            wageAmount = wageAmount,
            workersCount = workersCount,
            dressCode = dressCode,
            instructions = instructions,
            location = location,
            time = time,
            zone = zone
        )
        showSnackbar("Shift posted! ₹${newJob.securityDeposit.toInt()} anti-fake deposit secured.")
        notificationService?.notifyNewJobAlert(newJob)
        setBottomNavIndex(0)
    }

    fun openEmergencyHiringDialog(open: Boolean) {
        _uiState.update { it.copy(isEmergencyHiringDialogOpen = open) }
    }

    fun postEmergency30MinHiring(
        title: String,
        category: JobCategory,
        wageAmount: Double,
        workersCount: Int
    ) {
        val emergencyJob = repository.postEmergency30MinHiring(
            title = title,
            category = category,
            wageAmount = wageAmount,
            workersCount = workersCount
        )
        _uiState.update { it.copy(isEmergencyHiringDialogOpen = false) }
        showSnackbar("⚡ Emergency Broadcast active! 30-min timer started. Standby workers alerted.")
        notificationService?.notifyNewJobAlert(emergencyJob)
        setBottomNavIndex(0)
    }

    fun setWorkerJobTab(tab: WorkerJobTab) {
        _uiState.update { it.copy(workerActiveTab = tab) }
    }

    fun toggleSaveJob(jobId: String) {
        val isSaved = repository.toggleSaveJob(jobId)
        showSnackbar(if (isSaved) "Shift bookmarked in Saved tab." else "Shift removed from bookmarks.")
    }

    fun withdrawJobApplication(jobId: String) {
        val success = repository.withdrawJobApplication(jobId)
        if (success) {
            showSnackbar("Application withdrawn. Reserved surety hold released.")
        }
    }

    fun openSuretyDialog(open: Boolean) {
        _uiState.update { it.copy(isSuretyDialogOpen = open) }
    }

    fun topUpSuretyBalance(amount: Double) {
        val ok = repository.topUpSuretyBalance(amount)
        _uiState.update { it.copy(isSuretyDialogOpen = false) }
        if (ok) {
            showSnackbar("₹${amount.toInt()} added to Surety Escrow reserve.")
        }
    }

    fun withdrawSuretyBalance(amount: Double) {
        val ok = repository.withdrawSuretyBalance(amount)
        _uiState.update { it.copy(isSuretyDialogOpen = false) }
        if (ok) {
            showSnackbar("₹${amount.toInt()} refunded from Surety Escrow to standard wallet.")
        } else {
            showSnackbar("Withdrawal failed: amount is currently locked for active shifts.")
        }
    }

    // ---------------- Owner Job Management ----------------

    fun openEditJobDialog(job: JobPosting) {
        _uiState.update { it.copy(isEditJobDialogOpen = true, selectedJobForEdit = job) }
    }

    fun closeEditJobDialog() {
        _uiState.update { it.copy(isEditJobDialogOpen = false, selectedJobForEdit = null) }
    }

    fun saveEditedJob(
        jobId: String,
        title: String,
        category: JobCategory,
        wageAmount: Double,
        workersRequired: Int,
        time: String,
        dressCode: String,
        location: String,
        instructions: String
    ) {
        val ok = repository.editJob(
            jobId = jobId,
            title = title,
            category = category,
            wageAmount = wageAmount,
            workersRequired = workersRequired,
            time = time,
            dressCode = dressCode,
            location = location,
            instructions = instructions
        )
        closeEditJobDialog()
        if (ok) {
            showSnackbar("Shift details updated successfully.")
        }
    }

    fun deleteJob(jobId: String) {
        val ok = repository.deleteJob(jobId)
        if (ok) {
            showSnackbar("Shift deleted. Deposit refunded to business escrow.")
        }
    }

    fun toggleJobPause(jobId: String) {
        val isPaused = repository.toggleJobPause(jobId)
        showSnackbar(if (isPaused) "Shift paused. Not visible to candidates." else "Shift resumed and live.")
    }

    fun repostJob(jobId: String) {
        val newJob = repository.repostJob(jobId)
        if (newJob != null) {
            showSnackbar("Shift reposted with fresh timestamp!")
            notificationService?.notifyNewJobAlert(newJob)
        }
    }

    fun openReportNoShowDialog(applicant: JobApplicant) {
        _uiState.update { it.copy(isReportNoShowDialogOpen = true, selectedApplicantForNoShow = applicant) }
    }

    fun closeReportNoShowDialog() {
        _uiState.update { it.copy(isReportNoShowDialogOpen = false, selectedApplicantForNoShow = null) }
    }

    fun submitReportNoShow(applicantId: String, reason: String) {
        val ok = repository.reportWorkerNoShow(applicantId, reason)
        closeReportNoShowDialog()
        if (ok) {
            showSnackbar("No-Show logged: Worker's ₹100 surety forfeited & credited to you as compensation.")
        }
    }

    fun openRateWorkerDialog(applicant: JobApplicant) {
        _uiState.update { it.copy(isRateWorkerDialogOpen = true, selectedApplicantForRating = applicant) }
    }

    fun closeRateWorkerDialog() {
        _uiState.update { it.copy(isRateWorkerDialogOpen = false, selectedApplicantForRating = null) }
    }

    fun submitWorkerRating(applicantId: String, rating: Double, feedback: String) {
        val ok = repository.rateWorker(applicantId, rating, feedback)
        closeRateWorkerDialog()
        if (ok) {
            showSnackbar("Rating submitted! Thank you for rating the worker.")
        }
    }

    fun hireApplicant(applicantId: String) {
        repository.hireApplicant(applicantId)
        showSnackbar("Worker hired! Shift confirmed & check-in QR issued.")
    }

    fun rejectApplicant(applicantId: String) {
        repository.rejectApplicant(applicantId)
        showSnackbar("Applicant declined.")
    }

    fun openCheckInScanner(open: Boolean) {
        _uiState.update { it.copy(isCheckInScannerOpen = open) }
    }

    fun performWorkerCheckIn() {
        val worker = workerProfile.value
        repository.performWorkerCheckIn()
        _uiState.update { it.copy(isCheckInScannerOpen = false) }
        showSnackbar("Geo-Fence & QR Verified! Shift timer running.")
        notificationService?.notifyWorkerCheckInAlert(
            workerName = worker.name,
            jobTitle = "Senior Catering Steward",
            venueName = "Royal Feast Caterers (Banjara Hills)",
            time = "5:02 PM"
        )
    }

    fun performWorkerCheckOut() {
        val worker = workerProfile.value
        repository.performWorkerCheckOut()
        showSnackbar("Check-Out confirmed! Shift completed. Payout queued.")
        notificationService?.notifyWorkerCheckOutAlert(
            workerName = worker.name,
            jobTitle = "Senior Catering Steward",
            venueName = "Royal Feast Caterers",
            shiftPay = 850.0
        )
    }

    fun triggerOwnerAutoPayout(amount: Double) {
        repository.triggerOwnerAutoPayout(amount)
        showSnackbar("Shift closed! Instant UPI payout of ₹${amount.toInt()} disbursed.")
    }

    fun openWithdrawDialog(open: Boolean) {
        _uiState.update { it.copy(isWithdrawDialogOpen = open) }
    }

    fun withdrawWallet(
        amount: Double,
        method: String = "UPI",
        destination: String = ""
    ) {
        val success = repository.withdrawWorkerWallet(amount, method, destination)
        _uiState.update { it.copy(isWithdrawDialogOpen = false) }
        if (success) {
            val destInfo = if (destination.isNotBlank()) " ($destination)" else ""
            if (method.contains("Bank", ignoreCase = true)) {
                showSnackbar("₹${amount.toInt()} transferred to your bank account$destInfo via instant IMPS!")
            } else {
                showSnackbar("₹${amount.toInt()} transferred to your UPI account$destInfo instantly!")
            }
        } else {
            showSnackbar("Withdrawal failed: Amount exceeds available balance or is invalid.")
        }
    }

    fun openDisputeDialog(open: Boolean) {
        _uiState.update { it.copy(isDisputeDialogOpen = open) }
    }

    fun submitDispute(reason: String, target: String, details: String) {
        val roleStr = when (_uiState.value.role) {
            AppRole.WORKER -> "Worker"
            AppRole.OWNER -> "Business Owner"
            AppRole.ADMIN -> "Admin Moderator"
            AppRole.SUPER_ADMIN -> "Super Admin"
        }
        val name = when (_uiState.value.role) {
            AppRole.WORKER -> workerProfile.value.name
            AppRole.OWNER -> businessProfile.value.businessName
            else -> "Compliance Team"
        }
        repository.fileDispute(
            reportedByRole = roleStr,
            reporterName = name,
            target = target,
            reason = reason,
            details = details
        )
        _uiState.update { it.copy(isDisputeDialogOpen = false) }
        showSnackbar("Dispute ticket registered. Evidence & GPS logs logged.")
    }

    // ---------------- Admin & Super Admin Actions ----------------

    fun updatePlatformSettings(newSettings: PlatformGlobalSettings) {
        repository.updatePlatformSettings(newSettings)
        showSnackbar("Global settings updated: Commission ${newSettings.commissionRatePercent}%, Deposit ₹${newSettings.securityDepositPerJob.toInt()}")
    }

    fun resolveDispute(disputeId: String, ruling: String, releasePayment: Boolean) {
        repository.resolveDispute(disputeId, ruling, releasePayment)
        showSnackbar("Dispute #$disputeId adjudicated: ${if (releasePayment) "Worker Paid" else "Refunded to Owner"}")
    }

    fun toggleBusinessVerification() {
        val status = repository.toggleBusinessVerification()
        showSnackbar(if (status) "Business verified! Location & GSTIN badge active." else "Verification status set to pending.")
    }

    fun toggleWorkerVerification() {
        val status = repository.toggleWorkerVerification()
        showSnackbar(if (status) "Worker verified! Level 3 Face Selfie approved." else "Worker verification reset.")
    }

    fun banUser(userName: String, reason: String) {
        repository.banUser(userName, reason)
        showSnackbar("Account for $userName suspended: $reason")
    }

    // ---------------- Chat System ----------------

    fun openChatWithApplicant(applicant: JobApplicant) {
        val seedMessages = listOf(
            ChatMessage(applicant.workerName, "Hi, I am ready for the upcoming shift.", "10 mins ago", false),
            ChatMessage("You", "Welcome! Please make sure to adhere to the black shoes and formal uniform.", "8 mins ago", true),
            ChatMessage(applicant.workerName, "Understood! I will check in using QR upon arrival.", "Just now", false)
        )
        _uiState.update {
            it.copy(
                isChatSheetOpen = true,
                activeChatApplicant = applicant,
                chatMessages = seedMessages
            )
        }
    }

    fun closeChatSheet() {
        _uiState.update { it.copy(isChatSheetOpen = false, activeChatApplicant = null) }
    }

    fun sendChatMessage(text: String, isLocation: Boolean = false) {
        if (text.isBlank()) return
        val newMsg = ChatMessage(
            senderName = "You",
            text = text,
            timestamp = "Just now",
            isFromMe = true,
            isLocationShare = isLocation
        )
        _uiState.update { it.copy(chatMessages = it.chatMessages + newMsg) }
    }

    fun sendTestJobOpeningAlert() {
        val sampleJob = jobs.value.firstOrNull() ?: return
        val sent = notificationService?.notifyNewJobAlert(sampleJob) ?: false
        if (sent) {
            showSnackbar("Nearby Job Notification pushed to your device!")
        } else {
            showSnackbar("Notification triggered")
        }
    }

    fun sendTestApplicantAlert() {
        val sampleApp = applicants.value.firstOrNull() ?: return
        val sent = notificationService?.notifyNewApplicantAlert(sampleApp, sampleApp.jobTitle) ?: false
        if (sent) {
            showSnackbar("Applicant Alert pushed to your device!")
        } else {
            showSnackbar("Notification triggered")
        }
    }

    fun sendTestCheckInAlert() {
        val sent = notificationService?.notifyWorkerCheckInAlert(
            workerName = workerProfile.value.name,
            jobTitle = "Senior Catering Steward",
            venueName = "Royal Feast Caterers (Banjara Hills)",
            time = "5:02 PM"
        ) ?: false
        if (sent) {
            showSnackbar("Worker Check-In Alert pushed to your device!")
        } else {
            showSnackbar("Notification triggered")
        }
    }

    // ---------------- Referral & Commission Methods ----------------

    fun openReferDialog(open: Boolean) {
        _uiState.update { it.copy(isReferDialogOpen = open) }
    }

    fun referWorkerFriend(name: String, phone: String = "") {
        val friend = repository.referWorkerFriend(name, phone)
        showSnackbar("🎉 Referred ${friend.name}! You unlocked 2 Zero-Commission Withdrawals (Save 9.7%) + 2.3% bonus!")
    }

    fun simulateFriendEarnings(friendId: String, earnings: Double = 1000.0) {
        repository.simulateFriendEarnings(friendId, earnings)
        val bonus = earnings * 0.023
        showSnackbar("🎁 ₹${"%.2f".format(bonus)} (2.3% referral bonus from friend's shift) added to your wallet!")
    }

    fun referBusinessOwner(businessName: String, phone: String = "") {
        val owner = repository.referBusinessOwner(businessName, phone)
        showSnackbar("🎉 Referred ${owner.businessName}! You unlocked 2 Zero-Commission Shifts (100% Surety Refund) + 3.0% commission!")
    }

    fun simulateOwnerSuretyReferralBonus(ownerId: String) {
        repository.simulateOwnerSuretyReferralBonus(ownerId)
        showSnackbar("🎁 ₹9.00 (3.0% commission on referred owner's ₹300 surety fee) credited to your earnings!")
    }

    fun applyReferralCode(code: String) {
        val isWorker = _uiState.value.role == AppRole.WORKER
        val ok = repository.applyReferralCode(code, isWorker)
        if (ok) {
            if (isWorker) {
                showSnackbar("🎉 Code applied! 2 Zero-Commission Withdrawals unlocked (Save 9.7% each)!")
            } else {
                showSnackbar("🎉 Code applied! 2 Zero-Commission Shifts unlocked (100% Surety Refund)!")
            }
        } else {
            showSnackbar("Please enter a valid referral code.")
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun selectLanguage(language: AppLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
        repository.updateWorkerLanguage(language)
        val notice = when (language) {
            AppLanguage.ENGLISH -> "Language changed to English"
            AppLanguage.TELUGU -> "భాష తెలుగుకి మార్చబడింది (Telugu set as active language)"
            AppLanguage.HINDI -> "भाषा बदलकर हिन्दी कर दी गई है (Hindi set as active language)"
        }
        showSnackbar(notice)
        repository.addNotification(
            title = when (language) {
                AppLanguage.ENGLISH -> "Language Updated"
                AppLanguage.TELUGU -> "భాష నవీకరించబడింది"
                AppLanguage.HINDI -> "भाषा अपडेट की गई"
            },
            message = notice,
            type = "SYSTEM"
        )
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // ---------------- User Verification & DB Persistence ----------------

    fun initDatabase(db: DailyCrewDatabase) {
        repository.initDatabase(db)
    }

    fun openVerificationDialog(open: Boolean) {
        _uiState.update { it.copy(isVerificationDialogOpen = open) }
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
    ) {
        val record = repository.submitWorkerVerification(
            fullName = fullName,
            docType = docType,
            docNumber = docNumber,
            secondaryDocType = secondaryDocType,
            secondaryDocNumber = secondaryDocNumber,
            dob = dob,
            address = address,
            zone = zone,
            frontPhotoUri = frontPhotoUri,
            backPhotoUri = backPhotoUri,
            selfiePhotoUri = selfiePhotoUri
        )
        showSnackbar("🎉 Verified in Database! Record ID: ${record.id}")
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
    ) {
        val record = repository.submitBusinessVerification(
            legalBusinessName = legalBusinessName,
            docType = docType,
            docNumber = docNumber,
            secondaryDocType = secondaryDocType,
            secondaryDocNumber = secondaryDocNumber,
            registeredAddress = registeredAddress,
            zone = zone,
            businessType = businessType,
            docProofUri = docProofUri,
            ownerSignatoryName = ownerSignatoryName,
            ownerSignatoryPhone = ownerSignatoryPhone
        )
        showSnackbar("🎉 Business Marked as Verified in Database! (ID: ${record.id})")
    }
}
