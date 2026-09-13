package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VerificationRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DailyCrewDatabase : RoomDatabase() {
    abstract fun verificationDao(): VerificationDao

    companion object {
        @Volatile
        private var INSTANCE: DailyCrewDatabase? = null

        fun getDatabase(context: Context): DailyCrewDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DailyCrewDatabase::class.java,
                    "dailycrew_master.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
