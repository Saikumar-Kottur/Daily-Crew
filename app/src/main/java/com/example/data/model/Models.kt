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
    WAREHOUSE("Warehouse Staff", "warehouse"),
    HOUSEKEEPING("Housekeeping", "housekeeping"),
    RETAIL("Retail & Store", "retail"),
    DELIVERY("Delivery Assistance", "delivery"),
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

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val scriptSample: String,
    val flagEmoji: String
) {
    ENGLISH("en", "English", "English", "Welcome - Ready for Instant Shifts", "🇬🇧"),
    TELUGU("te", "Telugu", "తెలుగు", "స్వాగతం - తక్షణ షిఫ్ట్‌లు సిద్ధంగా ఉన్నాయి", "🇮🇳"),
    HINDI("hi", "Hindi", "हिन्दी", "स्वागत है - तत्काल शिफ्ट्स उपलब्ध हैं", "🇮🇳")
}

enum class ApplicantStatus {
    PENDING,
    SHORTLISTED,
    HIRED,
    REJECTED,
    CHECKED_IN,
    COMPLETED
}

enum class WorkerJobTab(val label: String, val iconName: String) {
    NEARBY("Nearby", "place"),
    RECOMMENDED("AI Match", "auto_awesome"),
    APPLIED("Applied", "assignment"),
    ACCEPTED("Accepted", "check_circle"),
    COMPLETED("Completed", "history"),
    SAVED("Saved", "bookmark")
}

data class SuretyTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String,
    val type: String, // "DEPOSIT", "LOCKED", "UNLOCKED_REFUNDED", "FORFEITED"
    val relatedJobTitle: String,
    val note: String
)

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
    val securityDeposit: Double = 300.0,
    val commissionPercent: Double = 6.5,
    val description: String = "Provide table setup, banquet beverage replenishment, and guest dining assistance with professional protocol.",
    val isSaved: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val suretyRequired: Double = 100.0,
    val aiMatchScore: Int = 94,
    val aiHighlights: String = "High match for your hospitality badge & high on-time score"
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
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val experience: String = "3.5 Years Hospitality Experience",
    val age: Int = 24,
    val categoryRole: String = "Senior Banquet Steward",
    val bio: String = "Punctual, verified catering steward with 3+ years experience across luxury hotels and banquet events in Hyderabad.",
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
    // Wallet & Surety Escrow
    val walletBalance: Double = 12450.0,
    val pendingPayout: Double = 850.0,
    val suretyEscrowBalance: Double = 600.0,
    val lockedSuretyAmount: Double = 100.0,
    val savedJobIds: List<String> = listOf("2", "4"),
    val activeShiftId: String? = "3",
    val isCheckedIn: Boolean = false,
    val checkInTimestamp: String? = null,
    // Trust, ID & Extended Profile Details
    val memberSince: String = "March 2024",
    val workerIdTag: String = "DC-WRK-49821",
    val aadhaarMasked: String = "XXXX XXXX 7391",
    val policeVerificationId: String = "TSP-HYD-2026-9812",
    val faceMatchConfidence: Double = 99.4,
    val isVerifiedInDb: Boolean = true,
    val verifiedDocumentType: String = "Aadhaar Card (UIDAI)",
    val verifiedDbRecordId: String = "REC-WRK-7391-DB",
    val verifiedDateFormatted: String = "Verified & Synced to Database",
    val documentFrontUri: String? = null,
    val documentBackUri: String? = null,
    val selfiePhotoUri: String? = null,
    val totalHoursWorked: Int = 348,
    val repeatHireRate: Int = 82,
    val workHistory: List<WorkerShiftHistoryItem> = defaultWorkerHistory(),
    val skillBadgesList: List<WorkerSkillBadge> = defaultWorkerSkillBadges(),
    // Referral & Commission Perks
    val referralCode: String = "DCW12345",
    val freeCommissionWithdrawalsCount: Int = 2, // 2 free commission withdrawals per friend referred
    val totalReferralsCount: Int = 1,
    val totalReferralEarnings: Double = 17.85, // 2.3% of platform commission generated from friend's earnings
    val referralFriends: List<ReferralFriendItem> = defaultReferralFriends()
) {
    val trustLevel: TrustLevel
        get() = when {
            trustScore >= 81 -> TrustLevel.ELITE
            trustScore >= 61 -> TrustLevel.TRUSTED
            trustScore >= 41 -> TrustLevel.AVERAGE
            else -> TrustLevel.RISKY
        }

    val isFullyVerified: Boolean
        get() = isPhoneVerified && isGoogleVerified && isSelfieVerified && isPoliceVerified && isAadhaarVerified
}

data class WorkerShiftHistoryItem(
    val id: String,
    val roleTitle: String,
    val businessName: String,
    val category: String,
    val date: String,
    val shiftDuration: String,
    val earningsAmount: Double,
    val ratingGiven: Double,
    val supervisorReview: String,
    val location: String,
    val isVerifiedCheckout: Boolean = true,
    val payoutStatus: String = "Escrow Released via UPI",
    val keyStrengths: List<String> = listOf("Punctual", "Uniform Compliant", "Polite")
)

data class WorkerSkillBadge(
    val id: String,
    val name: String,
    val category: String, // "Hospitality", "Food Safety", "Beverage", "Operations"
    val level: String, // "Level 3 - Certified", "Master Steward", "Accredited"
    val iconEmoji: String,
    val endorsements: Int,
    val verifiedBy: String,
    val dateAwarded: String,
    val isAccredited: Boolean = true
)

fun defaultWorkerHistory(): List<WorkerShiftHistoryItem> = listOf(
    WorkerShiftHistoryItem(
        id = "hist_1",
        roleTitle = "Banquet Captain & Lead Steward",
        businessName = "Royal Feast Caterers",
        category = "Catering & Banquet",
        date = "Yesterday",
        shiftDuration = "8 Hours (12:00 PM - 08:00 PM)",
        earningsAmount = 900.0,
        ratingGiven = 5.0,
        supervisorReview = "Ramesh led the buffet floor with exceptional etiquette. Arrived in sharp uniform 20 minutes early. Zero breakage and immaculate plate clearing.",
        location = "Banjara Hills, Hyderabad",
        isVerifiedCheckout = true,
        payoutStatus = "₹900 Instant UPI Escrow Released",
        keyStrengths = listOf("Early Arrival", "Flawless Etiquette", "FSSAI Compliant")
    ),
    WorkerShiftHistoryItem(
        id = "hist_2",
        roleTitle = "Fine Dining Table Steward",
        businessName = "Spice Bazaar Restaurant",
        category = "Restaurant Staff",
        date = "3 days ago",
        shiftDuration = "7 Hours (04:00 PM - 11:00 PM)",
        earningsAmount = 700.0,
        ratingGiven = 4.9,
        supervisorReview = "Flawless table service during peak dinner rush. Highly dependable, prompt guest communication, and zero kitchen order mix-ups.",
        location = "Habsiguda, Hyderabad",
        isVerifiedCheckout = true,
        payoutStatus = "₹700 Instant UPI Settled",
        keyStrengths = listOf("High Speed", "Courteous", "Punctual")
    ),
    WorkerShiftHistoryItem(
        id = "hist_3",
        roleTitle = "VIP Wedding Banquet Server",
        businessName = "Elite Celebrations",
        category = "Event Staff",
        date = "Last week",
        shiftDuration = "10 Hours (02:00 PM - 12:00 AM)",
        earningsAmount = 1850.0,
        ratingGiven = 5.0,
        supervisorReview = "Outstanding guest communication and zero table delays across 250 VIP tables. Requested as lead steward for our next corporate gala.",
        location = "Tarnaka, Hyderabad",
        isVerifiedCheckout = true,
        payoutStatus = "₹1,850 Instant UPI Settled",
        keyStrengths = listOf("VIP Protocol", "Great Teamwork", "Zero No-Show")
    ),
    WorkerShiftHistoryItem(
        id = "hist_4",
        roleTitle = "Kitchen Operations Helper",
        businessName = "Grand Palace Banquets",
        category = "Kitchen Helper",
        date = "2 weeks ago",
        shiftDuration = "8 Hours (09:00 AM - 05:00 PM)",
        earningsAmount = 850.0,
        ratingGiven = 4.8,
        supervisorReview = "Rapid dish turnaround and excellent food hygiene compliance. Solid, honest worker who supports team members proactively.",
        location = "Uppal, Hyderabad",
        isVerifiedCheckout = true,
        payoutStatus = "₹850 Settled in Bank",
        keyStrengths = listOf("Hygiene Master", "Hard Worker", "Zero Waste")
    ),
    WorkerShiftHistoryItem(
        id = "hist_5",
        roleTitle = "Buffet Steward & Hygiene Coordinator",
        businessName = "Southern Spice Conclave",
        category = "Catering Staff",
        date = "3 weeks ago",
        shiftDuration = "6 Hours (05:00 PM - 11:00 PM)",
        earningsAmount = 650.0,
        ratingGiven = 4.9,
        supervisorReview = "Strict FSSAI temperature logging and great coordination with the head chef. Perfect hygiene checklist execution.",
        location = "Nacharam, Hyderabad",
        isVerifiedCheckout = true,
        payoutStatus = "₹650 Instant UPI Settled",
        keyStrengths = listOf("FSSAI Trained", "Reliable", "Proactive")
    )
)

fun defaultWorkerSkillBadges(): List<WorkerSkillBadge> = listOf(
    WorkerSkillBadge(
        id = "sk_1",
        name = "Catering Steward",
        category = "Hospitality Service",
        level = "Level 3 - Certified",
        iconEmoji = "🍽️",
        endorsements = 38,
        verifiedBy = "DailyCrew Hospitality Auditor",
        dateAwarded = "Jan 2025",
        isAccredited = true
    ),
    WorkerSkillBadge(
        id = "sk_2",
        name = "Table Service & Banqueting",
        category = "Guest Experience",
        level = "Master Steward",
        iconEmoji = "🍷",
        endorsements = 42,
        verifiedBy = "Royal Feast Caterers & Elite Banquets",
        dateAwarded = "Feb 2025",
        isAccredited = true
    ),
    WorkerSkillBadge(
        id = "sk_3",
        name = "Food Safety & Hygiene (FSSAI)",
        category = "Compliance & Safety",
        level = "Certified Compliant",
        iconEmoji = "🧼",
        endorsements = 45,
        verifiedBy = "FSSAI Partner Training Institute",
        dateAwarded = "Dec 2024",
        isAccredited = true
    ),
    WorkerSkillBadge(
        id = "sk_4",
        name = "Beverage & Barista Prep",
        category = "Beverage Operations",
        level = "Level 2 - Verified",
        iconEmoji = "☕",
        endorsements = 19,
        verifiedBy = "4 Verified Cafe Employers",
        dateAwarded = "Nov 2024",
        isAccredited = false
    ),
    WorkerSkillBadge(
        id = "sk_5",
        name = "Inventory & Stock Counting",
        category = "Logistics & Store",
        level = "Level 2 - Verified",
        iconEmoji = "📦",
        endorsements = 14,
        verifiedBy = "Grand Palace Store Auditor",
        dateAwarded = "Oct 2024",
        isAccredited = false
    ),
    WorkerSkillBadge(
        id = "sk_6",
        name = "VIP Dining Protocol",
        category = "Luxury Hospitality",
        level = "Master Protocol",
        iconEmoji = "👔",
        endorsements = 28,
        verifiedBy = "Elite Celebrations Corporate",
        dateAwarded = "Jan 2025",
        isAccredited = true
    )
)


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
    val isGstinVerified: Boolean = true,
    val isVerifiedInDb: Boolean = true,
    val verifiedDocumentType: String = "GSTIN Certificate",
    val verifiedDbRecordId: String = "REC-BIZ-36AA-DB",
    val verifiedDateFormatted: String = "Verified & Synced to Database",
    val documentFrontUri: String? = null,
    val rating: Double = 4.7,
    val reliabilityScore: Int = 96,
    val trustScore: Int = 94,
    val fakeJobFlags: Int = 0,
    val activeShiftCount: Int = 2,
    val totalWorkersHired: Int = 184,
    val totalJobsCreated: Int = 28,
    val completionRatePercent: Int = 98,
    val workerAvgRating: Double = 4.8,
    val totalPayoutsDisbursed: Double = 168500.0,
    val escrowSecurityDepositBalance: Double = 2500.0,
    val badges: List<String> = listOf("Trusted Employer", "Fast Payer", "Top Rated"),
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    // Referral & Commission Perks
    val referralCode: String = "DCO12345",
    val freeCommissionJobsCount: Int = 2, // 2 free commission jobs per referred business owner (100% surety refund)
    val totalReferralsCount: Int = 1,
    val totalReferralEarnings: Double = 24.12, // 3.0% of DailyCrew's surety revenue generated from referred owners
    val referralOwners: List<ReferralOwnerItem> = defaultReferralOwners(),
    val isReferredBySomeone: Boolean = true
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
    val distanceKm: Double = 1.4,
    val skills: List<String> = listOf("Catering", "Hospitality")
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
    val commissionRatePercent: Double = 6.5, // 5% to 8% standard range
    val securityDepositPerJob: Double = 300.0, // ₹300 owner surety fee per job in escrow
    val workerWithdrawalCommissionPercent: Double = 9.7, // 9.7% commission deducted at worker withdrawal time (90.3% to worker)
    val ownerSuretyRefundPercent: Double = 73.2, // 73.2% (₹219.60) returned to owner on shift completion
    val ownerSuretyPlatformCommissionPercent: Double = 26.8, // 26.8% (₹80.40) becomes DailyCrew platform revenue
    val workerReferralBonusPercent: Double = 2.3, // 2.3% of platform commission generated from referred worker
    val ownerReferralBonusPercent: Double = 3.0, // 3.0% of DailyCrew's surety revenue generated from referred owner
    val totalPlatformRevenueCollected: Double = 8940.0, // Total platform commission credited to DailyCrew
    // Admin Wallet Analytics
    val totalWorkerCommissionsCollected: Double = 5238.0, // 9.7% cuts on worker withdrawals
    val totalSuretyRevenueCollected: Double = 3702.0, // 26.8% cuts on ₹300 owner surety fees
    val totalReferralPayoutsDisbursed: Double = 462.0, // Total referral rewards transferred to referrers
    val dailyRevenue: Double = 4280.0,
    val monthlyRevenue: Double = 128400.0,
    val totalPlatformWorkersCount: Int = 1480,
    val totalPlatformOwnersCount: Int = 340,
    val totalCompletedJobsCount: Int = 842,
    val pilotZones: List<String> = listOf("Uppal", "Habsiguda", "Tarnaka", "Nacharam"),
    val autoEscrowPayoutEnabled: Boolean = true,
    val emergencyHiringRadiusKm: Double = 5.0,
    val antiNoShowPenaltyScore: Int = 15,
    val defaultAppLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isUnderMaintenance: Boolean = false
) {
    val netProfit: Double get() = (totalWorkerCommissionsCollected + totalSuretyRevenueCollected) - totalReferralPayoutsDisbursed
}

data class ReferralFriendItem(
    val id: String,
    val name: String,
    val role: String,
    val joinedDate: String,
    val shiftsCompleted: Int,
    val friendEarnings: Double,
    val bonusEarned: Double // 2.3% of platform commission generated from friend
)

data class ReferralOwnerItem(
    val id: String,
    val businessName: String,
    val joinedDate: String,
    val jobsPosted: Int,
    val bonusEarned: Double // 3.0% of DailyCrew's surety revenue from referred owner
)

fun defaultReferralFriends(): List<ReferralFriendItem> = listOf(
    ReferralFriendItem(
        id = "ref_f1",
        name = "Suresh Reddy",
        role = "Senior Banquet Steward",
        joinedDate = "3 days ago",
        shiftsCompleted = 8,
        friendEarnings = 8000.0,
        bonusEarned = 17.85 // 8000 * 9.7% commission = ₹776. 2.3% of ₹776 = ₹17.85
    )
)

fun defaultReferralOwners(): List<ReferralOwnerItem> = listOf(
    ReferralOwnerItem(
        id = "ref_o1",
        businessName = "Spice Garden Banquets",
        joinedDate = "1 week ago",
        jobsPosted = 10,
        bonusEarned = 24.12 // 10 jobs * (₹300 * 26.8% platform rev = ₹80.40) * 3.0% = ₹24.12
    )
)

