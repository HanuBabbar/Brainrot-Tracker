package com.rogue.brainrottracker.data.remote

import com.rogue.brainrottracker.data.model.FriendsResponse
import com.rogue.brainrottracker.data.model.UserSearchResult
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// fromUserId removed — the server reads the caller identity from the JWT Bearer token
@Serializable
data class FriendActionRequest(val toUserId: String)

// userId fields removed — server reads caller identity from the JWT Bearer token
@Serializable
data class AcceptDeclineRequest(val friendUserId: String)

@Serializable
data class RemoveFriendRequest(val friendUserId: String)

object FriendsApiService {
    private val client get() = NetworkClient.client
    private val base get() = NetworkClient.BASE_URL + "friends"

    suspend fun searchByCode(code: String): Result<UserSearchResult> = runCatching {
        client.get("$base/search") {
            parameter("code", code)
        }.body()
    }

    suspend fun sendRequest(toUserId: String): Result<Unit> = runCatching {
        client.post("$base/request") {
            contentType(ContentType.Application.Json)
            setBody(FriendActionRequest(toUserId))
        }
        Unit
    }

    suspend fun acceptRequest(friendUserId: String): Result<Unit> = runCatching {
        client.post("$base/accept") {
            contentType(ContentType.Application.Json)
            setBody(AcceptDeclineRequest(friendUserId))
        }
        Unit
    }

    suspend fun declineRequest(friendUserId: String): Result<Unit> = runCatching {
        client.post("$base/decline") {
            contentType(ContentType.Application.Json)
            setBody(AcceptDeclineRequest(friendUserId))
        }
        Unit
    }

    suspend fun removeFriend(friendUserId: String): Result<Unit> = runCatching {
        client.delete("$base/remove") {
            contentType(ContentType.Application.Json)
            setBody(RemoveFriendRequest(friendUserId))
        }
        Unit
    }

    suspend fun getFriends(userId: String): Result<FriendsResponse> = runCatching {
        client.get("$base/$userId").body()
    }
}
