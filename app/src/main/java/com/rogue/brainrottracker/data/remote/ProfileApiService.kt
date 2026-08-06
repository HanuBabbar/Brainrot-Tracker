package com.rogue.brainrottracker.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class ProfileUpdateRequest(
    val userId: String,
    val name: String
)

@Serializable
data class ProfileUpdateResponse(
    val message: String,
    val user: UserInfo? = null
)

@Serializable
data class UserInfo(
    val userId: String,
    val name: String,
    val emailId: String,
    val friendCode: String? = null
)

object ProfileApiService {
    private val client get() = NetworkClient.client
    private val base get() = NetworkClient.BASE_URL + "auth/profile"

    suspend fun updateProfileName(userId: String, name: String): Result<ProfileUpdateResponse> = runCatching {
        client.put(base) {
            contentType(ContentType.Application.Json)
            setBody(ProfileUpdateRequest(userId = userId, name = name))
        }.body()
    }
}
