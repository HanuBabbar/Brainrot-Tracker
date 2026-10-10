package com.rogue.brainrottracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val platform: String,
    val startTime: Long,
    val endTime: Long?,
    val swipeCount: Int
)
