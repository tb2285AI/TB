package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BriefingDao {
    @Query("SELECT * FROM intel_briefings ORDER BY timestamp DESC")
    fun getAllBriefings(): Flow<List<IntelBriefingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBriefing(briefing: IntelBriefingEntity): Long

    @Update
    suspend fun updateBriefing(briefing: IntelBriefingEntity)

    @Query("DELETE FROM intel_briefings WHERE id = :id")
    suspend fun deleteBriefingById(id: Int)

    @Query("SELECT * FROM intel_briefings WHERE id = :id LIMIT 1")
    suspend fun getBriefingById(id: Int): IntelBriefingEntity?
}
