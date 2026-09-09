package com.example.data.model

enum class AppRole(val displayName: String, val badge: String) {
    WORKER("Worker", "👷"),
    OWNER("Business Owner", "🏪"),
    ADMIN("Admin Moderator", "🛡️"),
    SUPER_ADMIN("Super Admin", "⚙️")
}

enum class JobCategory(val label: String, val iconName: String) {
    ALL("All Shifts", "all"),
    CANTEEN("Canteen Staff", "canteen"),
    SERVING("Serving Staff", "serving"),
    HOTEL("Hotel Staff", "hotel"),
    RESTAURANT("Restaurant Staff", "restaurant"),
    CATERING("Catering Staff", "catering"),
    EVENT("Event Staff", "event"),
    HOUSEKEEPING("Housekeeping", "housekeeping"),
    KITCHEN_HELPER("Kitchen Helper", "kitchen"),
    CLEANER("Cleaner", "cleaner"),
    CASHIER("Cashier", "cashier")
}

enum class BadgeTier(val title: String, val minScore: Int, val badgeColorHex: Long, val perk: String) {
    BRONZE("Bronze Tier", 0, 0xFFCD7F32, "Basic Verification"),
    SILVER("Silver Tier", 60, 0xFFC0C0C0, "Fast Payout Access"),
    GOLD("Gold Tier", 80, 0xFFFFD700, "Priority Dispatch"),
    PLATINUM("Platinum Elite", 92, 0xFF00E676, "Instant Hire & Zero Fee")
}

enum class TrustLevel(val label: String, val colorHex: Long, val description: String) {
    RISKY("Risky (0-40)", 0xFFFF5252, "Requires manual manager oversight"),
    AVERAGE("Average (41-60)", 0xFFFFAB00, "Standard verification verified"),
    TRUSTED("Trusted (61-80)", 0xFF00B0FF, "High attendance and positive reviews"),
    ELITE("Elite (81-100)", 0xFF00E676, "Top 5% reliability, zero no-shows")
}

enum class ApplicantStatus {
    PENDING,
    SHORTLISTED,
    HIRED,
    REJECTED,
    CHECKED_IN,
    COMPLETED
}

data class JobPosting(
    val id: String,
    val title: String,
    val businessName: String,
    val businessCategory: String,
    val wage: String,
    val unit: String = "/ shift (8 hrs)",
    val wageAmount: Double = 850.0,
    val distanceKm: Double = 1.2,
    val time: String,
    val date: String = "Today",
    val dressCode: String,
    val location: String,
    val zone: String = "Uppal", // Hyderabad Phase 1 zones: Uppal, Habsiguda, Tarnaka, Nacharam
    val instructions: String,
    val workersRequired: Int,
    val workersHired: Int = 0,
    val category: JobCategory = JobCategory.CATERING,
    val rating: Double = 4.8,
    val experienceRequired: String = "1+ year catering or hospitality",
    val isApplied: Boolean = false,
    val isUrgent: Boolean = false,
    val isEmergency30Min: Boolean = false,
    val securityDeposit: Double = 500.0,
    val commissionPercent: Double = 6.5
)

data class WorkerProfile(
    val id: String = "w1",
    val name: String = "Ramesh Kumar",
    val phone: String = "+91 98765 43210",
    val email: String = "ramesh.kumar@dailycrew.work",
    val avatarInitials: String = "RK",
    val rating: Double = 4.85,
    val completedJobs: Int = 47,
    val onTimeRate: Int = 98,
    val trustScore: Int = 96,
    val badgeTier: BadgeTier = BadgeTier.PLATINUM,
    val skills: List<String> = listOf("Catering Steward", "Table Service", "Banquet Setup", "Food Safety Hygiene"),
    val languages: List<String> = listOf("English", "Hindi", "Telugu"),
    val experience: String = "3.5 Years Hospitality Experience",
    // Verification Levels
    val isPhoneVerified: Boolean = true, // Level 1
    val isGoogleVerified: Boolean = true, // Level 2
    val isSelfieVerified: Boolean = true, // Level 3
    val isPoliceVerified: Boolean = true,
    val isAadhaarVerified: Boolean = true,
    // Standby Mode ("Available Now")
    val isAvailableNow: Boolean = true,
    val currentZone: String = "Habsiguda, Hyderabad",
    // Gamification & XP
    val xpPoints: Int = 3450,
    val streakDays: Int = 14,
    val achievementBadges: List<String> = listOf("Fast Responder", "Reliable Worker", "Top Performer", "Elite Worker"),
    // Wallet
    val walletBalance: Double = 12450.0,
    val pendingPayout: Double = 850.0,
    val activeShiftId: String? = null,
    val isCheckedIn: Boolean = false,
    val checkInTimestamp: String? = null
) {
    val trustLevel: TrustLevel
        get() = when {
            trustScore >= 81 -> TrustLevel.ELITE
            trustScore >= 61 -> TrustLevel.TRUSTED
            trustScore >= 41 -> TrustLevel.AVERAGE
            else -> TrustLevel.RISKY
        }
}

data class BusinessProfile(
    val id: String = "b1",
    val businessName: String = "Royal Feast Caterers",
    val category: String = "Banquet & Luxury Catering",
    val address: String = "Road No. 12, Banjara Hills, Hyderabad",
    val zone: String = "Uppal",
    val gstin: String = "36AAAAA0000A1Z5",
    // Verification Levels
    val isPhoneVerified: Boolean = true, // Level 1
    val isGoogleVerified: Boolean = true, // Level 2
    val isLocationVerified: Boolean = true, // Level 3
    val rating: Double = 4.7,
    val reliabilityScore: Int = 96,
    val trustScore: Int = 94,
    val fakeJobFlags: Int = 0,
    val activeShiftCount: Int = 2,
    val totalWorkersHired: Int = 184,
    val totalPayoutsDisbursed: Double = 168500.0,
    val escrowSecurityDepositBalance: Double = 2500.0,
    val badges: List<String> = listOf("Trusted Employer", "Fast Payer", "Top Rated")
) {
    val isVerified: Boolean get() = isPhoneVerified && isGoogleVerified && isLocationVerified

    val trustLevel: TrustLevel
        get() = when {
            trustScore >= 81 -> TrustLevel.ELITE
            trustScore >= 61 -> TrustLevel.TRUSTED
            trustScore >= 41 -> TrustLevel.AVERAGE
            else -> TrustLevel.RISKY
        }
}

data class JobApplicant(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val workerName: String,
    val role: String,
    val rating: Double,
    val jobsCompleted: Int,
    val onTimeRate: Int,
    val trustScore: Int,
    val isVerified: Boolean,
    val status: ApplicantStatus = ApplicantStatus.PENDING,
    val checkInTime: String? = null,
    val appliedAt: String = "15 mins ago",
    val isStandbyReady: Boolean = true,
    val distanceKm: Double = 1.4
)

data class PayoutTransaction(
    val id: String,
    val title: String,
    val businessOrWorker: String,
    val date: String,
    val grossAmount: Double,
    val platformFee: Double,
    val netAmount: Double,
    val isCredit: Boolean = true,
    val paymentMethod: String = "Instant UPI",
    val status: String = "Completed (Escrow Released)"
) {
    val amount: Double get() = netAmount
}

data class DisputeReport(
    val id: String,
    val reportedByRole: String,
    val reporterName: String,
    val targetName: String,
    val reason: String,
    val details: String,
    val evidenceLogs: List<String> = listOf("GPS Arrival: 17.4012° N, 78.5582° E", "QR Scan Timestamp: 05:02:14 PM", "Supervisor Chat Transcript"),
    val status: String = "Under Review", // "Under Review", "Resolved - Refunded", "Resolved - Released", "Dismissed"
    val timestamp: String = "10 mins ago",
    val adminRuling: String? = null
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String, // "JOB", "CHECKIN", "PAYOUT", "ALERT", "EMERGENCY"
    val isRead: Boolean = false
)

data class AiInsightItem(
    val title: String,
    val category: String,
    val description: String,
    val scoreText: String,
    val recommendation: String
)

data class PlatformGlobalSettings(
    val commissionRatePercent: Double = 6.5, // 5% to 8%
    val securityDepositPerJob: Double = 500.0,
    val pilotZones: List<String> = listOf("Uppal", "Habsiguda", "Tarnaka", "Nacharam"),
    val autoEscrowPayoutEnabled: Boolean = true,
    val emergencyHiringRadiusKm: Double = 5.0,
    val antiNoShowPenaltyScore: Int = 15,
    val isUnderMaintenance: Boolean = false
)
