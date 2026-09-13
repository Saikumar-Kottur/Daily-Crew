package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationDao {
    @Query("SELECT * FROM verification_records ORDER BY createdAtTimestamp DESC")
    fun getAllVerificationsFlow(): Flow<List<VerificationRecordEntity>>

    @Query("SELECT * FROM verification_records WHERE userId = :userId LIMIT 1")
    fun getVerificationForUserFlow(userId: String): Flow<VerificationRecordEntity?>

    @Query("SELECT * FROM verification_records WHERE userId = :userId AND userRole = :role LIMIT 1")
    suspend fun getVerificationRecord(userId: String, role: String): VerificationRecordEntity?

    @Query("SELECT * FROM verification_records")
    suspend fun getAllVerifications(): List<VerificationRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(record: VerificationRecordEntity)

    @Query("UPDATE verification_records SET verificationStatus = :status, isVerifiedInDb = :isVerified WHERE id = :recordId")
    suspend fun updateStatus(recordId: String, status: String, isVerified: Boolean)

    @Query("DELETE FROM verification_records WHERE id = :recordId")
    suspend fun deleteVerification(recordId: String)
}
