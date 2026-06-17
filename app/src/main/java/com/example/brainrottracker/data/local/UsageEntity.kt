package com.example.brainrottracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "usage_stats")
data class UsageEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int=0,
        val date: String,
        val platform: String,
        val count: Int
)