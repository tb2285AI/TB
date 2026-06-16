package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [IntelBriefingEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun briefingDao(): BriefingDao
}
