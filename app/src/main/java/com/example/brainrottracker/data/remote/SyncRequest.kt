package com.example.brainrottracker.data.remote

import com.example.brainrottracker.data.local.UsageEntity
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val userId: String,
    val stats: List<UsageEntity>
)
