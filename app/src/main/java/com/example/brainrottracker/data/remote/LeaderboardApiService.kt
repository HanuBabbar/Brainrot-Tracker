package com.example.brainrottracker.data.remote

import com.example.brainrottracker.data.model.LeaderboardResponse
import io.ktor.client.call.*
import io.ktor.client.request.*

object LeaderboardApiService {
    private val client get() = NetworkClient.client
    private val base get() = NetworkClient.BASE_URL + "leaderboard"

    suspend fun getGlobalLeaderboard(userId: String?): Result<LeaderboardResponse> = runCatching {
        client.get("$base/daily") {
            if (userId != null) parameter("userId", userId)
        }.body()
    }

    suspend fun getFriendsLeaderboard(userId: String): Result<LeaderboardResponse> = runCatching {
        client.get("$base/friends") {
            parameter("userId", userId)
        }.body()
    }
}
