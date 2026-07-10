package com.example.brainrottracker.data.remote

import com.example.brainrottracker.data.model.FriendsResponse
import com.example.brainrottracker.data.model.UserSearchResult
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class FriendActionRequest(val fromUserId: String, val toUserId: String)

@Serializable
data class AcceptDeclineRequest(val userId: String, val friendUserId: String)

@Serializable
data class RemoveFriendRequest(val userId: String, val friendUserId: String)

object FriendsApiService {
    private val client get() = NetworkClient.client
    private val base get() = NetworkClient.BASE_URL + "friends"

    suspend fun searchByCode(code: String): Result<UserSearchResult> = runCatching {
        client.get("$base/search") {
            parameter("code", code)
        }.body()
    }

    suspend fun sendRequest(fromUserId: String, toUserId: String): Result<Unit> = runCatching {
        client.post("$base/request") {
            contentType(ContentType.Application.Json)
            setBody(FriendActionRequest(fromUserId, toUserId))
        }
        Unit
    }

    suspend fun acceptRequest(userId: String, friendUserId: String): Result<Unit> = runCatching {
        client.post("$base/accept") {
            contentType(ContentType.Application.Json)
            setBody(AcceptDeclineRequest(userId, friendUserId))
        }
        Unit
    }

    suspend fun declineRequest(userId: String, friendUserId: String): Result<Unit> = runCatching {
        client.post("$base/decline") {
            contentType(ContentType.Application.Json)
            setBody(AcceptDeclineRequest(userId, friendUserId))
        }
        Unit
    }

    suspend fun removeFriend(userId: String, friendUserId: String): Result<Unit> = runCatching {
        client.delete("$base/remove") {
            contentType(ContentType.Application.Json)
            setBody(RemoveFriendRequest(userId, friendUserId))
        }
        Unit
    }

    suspend fun getFriends(userId: String): Result<FriendsResponse> = runCatching {
        client.get("$base/$userId").body()
    }
}
