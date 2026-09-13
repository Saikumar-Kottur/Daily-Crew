package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verification_records")
data class VerificationRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userRole: String, // "WORKER" or "OWNER"
    val legalName: String,
    val documentType: String, // e.g. "Aadhaar Card", "PAN Card", "GSTIN Certificate", etc.
    val documentNumber: String,
    val secondaryDocType: String? = null,
    val secondaryDocNumber: String? = null,
    val documentFrontUri: String = "",
    val documentBackUri: String = "",
    val selfiePhotoUri: String = "",
    val registeredAddress: String = "",
    val zone: String = "",
    val dateOfBirthOrReg: String = "",
    val verificationStatus: String = "VERIFIED", // "VERIFIED", "PENDING_REVIEW", "REJECTED"
    val isVerifiedInDb: Boolean = true,
    val verifiedDate: String,
    val verifiedBy: String = "DailyCrew Automated Trust Engine",
    val faceMatchConfidence: Double = 99.4,
    val notes: String = "Government ID & Biometric registry verified.",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
