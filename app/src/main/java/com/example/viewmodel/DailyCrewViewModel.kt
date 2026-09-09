package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.DailyCrewRepository
import com.example.notification.DailyCrewNotificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DailyCrewUiState(
    val role: AppRole = AppRole.WORKER,
    val bottomNavIndex: Int = 0,
    val searchQuery: String = "",
    val selectedCategory: JobCategory = JobCategory.ALL,
    val selectedZoneFilter: String = "All", // Pilot zones: Uppal, Habsiguda, Tarnaka, Nacharam
    val filterRadiusKm: Float = 10f,
    val minWageFilter: Float = 0f,
    val filterEmergencyOnly: Boolean = false,
    val filterVerifiedOnly: Boolean = false,
    val selectedJobForDetails: JobPosting? = null,
    val isCheckInScannerOpen: Boolean = false,
    val isWithdrawDialogOpen: Boolean = false,
    val isDisputeDialogOpen: Boolean = false,
    val isEmergencyHiringDialogOpen: Boolean = false,
    val isChatSheetOpen: Boolean = false,
    val activeChatApplicant: JobApplicant? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
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

            matchesQuery && matchesCategory && matchesRadius && matchesWage && matchesZone && matchesEmergency
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Job Matching Recommendations
    val aiRecommendedJobs: StateFlow<List<JobPosting>> = combine(
        repository.jobs,
        repository.workerProfile
    ) { allJobs, worker ->
        allJobs.sortedByDescending { job ->
            var score = 0
            if (worker.skills.any { job.title.contains(it, ignoreCase = true) || job.category.label.contains(it, ignoreCase = true) }) score += 40
            if (job.distanceKm < 2.0) score += 30
            if (job.rating >= 4.7) score += 20
            if (job.isUrgent || job.isEmergency30Min) score += 10
            score
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Top Verified Applicants for Owner
    val aiTopApplicants: StateFlow<List<JobApplicant>> = repository.applicants.map { list ->
        list.sortedByDescending { it.trustScore * 10 + (it.rating * 10).toInt() + if (it.isVerified) 50 else 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchRole(newRole: AppRole) {
        _uiState.update { it.copy(role = newRole, bottomNavIndex = 0) }
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

    fun withdrawWallet(amount: Double) {
        repository.withdrawWorkerWallet(amount)
        _uiState.update { it.copy(isWithdrawDialogOpen = false) }
        showSnackbar("₹${amount.toInt()} transferred to your UPI account instantly!")
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

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
