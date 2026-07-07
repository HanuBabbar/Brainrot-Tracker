package com.example.brainrottracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FriendProfile(
    val userId: String,
    val name: String,
    val friendCode: String,
    val todayTotal: Int = 0,
)

@Serializable
data class FriendRequest(
    val userId: String,
    val name: String,
    val friendCode: String,
)

@Serializable
data class FriendsResponse(
    val friends: List<FriendProfile> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
)

@Serializable
data class UserSearchResult(
    val userId: String,
    val name: String,
    val friendCode: String,
)

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val name: String,
    val totalCount: Int,
    val isMe: Boolean = false,
)

@Serializable
data class LeaderboardResponse(
    val date: String,
    val leaderboard: List<LeaderboardEntry>,
    val myRank: Int?,
    val myEntry: LeaderboardEntry? = null,
)
