package com.rogue.brainrottracker.data.remote

import com.rogue.brainrottracker.data.local.UsageEntity
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    // userId is no longer sent in the body — the server reads it from the JWT Bearer token
    val stats: List<UsageEntity>
)

@Serializable
data class PullStat(
    val date: String,
    val platform: String,
    val count: Int
)

@Serializable
data class PullResponse(
    val stats: List<PullStat>
)
