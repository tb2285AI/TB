package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intel_briefings")
data class IntelBriefingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val scenario: String,
    val region: String,
    val threatLevel: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userNotes: String = ""
)
