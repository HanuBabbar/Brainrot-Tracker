package com.rogue.brainrottracker.data.remote

import com.rogue.brainrottracker.data.local.UsageEntity
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val userId: String,
    val stats: List<UsageEntity>
)
