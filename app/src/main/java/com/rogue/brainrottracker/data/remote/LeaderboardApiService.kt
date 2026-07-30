package com.rogue.brainrottracker.data.remote

import com.rogue.brainrottracker.data.model.LeaderboardResponse
import io.ktor.client.call.*
import io.ktor.client.request.*

object LeaderboardApiService {
    private val client get() = NetworkClient.client
    private val base get() = NetworkClient.BASE_URL + "leaderboard"

    private fun getLocalDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }

    suspend fun getGlobalLeaderboard(userId: String?): Result<LeaderboardResponse> = runCatching {
        client.get("$base/daily") {
            if (userId != null) parameter("userId", userId)
            parameter("date", getLocalDate())
        }.body()
    }

    suspend fun getFriendsLeaderboard(userId: String): Result<LeaderboardResponse> = runCatching {
        client.get("$base/friends") {
            parameter("userId", userId)
            parameter("date", getLocalDate())
        }.body()
    }
}
