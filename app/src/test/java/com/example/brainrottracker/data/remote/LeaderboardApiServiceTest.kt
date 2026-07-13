package com.example.brainrottracker.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LeaderboardApiServiceTest {

    @Before
    fun setup() {
        mockkObject(NetworkClient)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getGlobalLeaderboard returns success on valid response`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "date": "2024-01-01",
                    "leaderboard": [
                        { "userId": "1", "name": "Test User", "totalCount": 100, "rank": 1 }
                    ],
                    "myRank": 1
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        every { NetworkClient.client } returns mockClient

        val result = LeaderboardApiService.getGlobalLeaderboard("1")
        
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals("2024-01-01", response?.date)
        assertEquals(1, response?.leaderboard?.size)
        assertEquals("Test User", response?.leaderboard?.first()?.name)
    }

    @Test
    fun `getGlobalLeaderboard returns failure on server error`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }
        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        every { NetworkClient.client } returns mockClient

        val result = LeaderboardApiService.getGlobalLeaderboard("1")
        
        assertTrue(result.isFailure)
    }
}
