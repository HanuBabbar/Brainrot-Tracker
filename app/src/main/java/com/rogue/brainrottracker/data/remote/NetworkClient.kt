package com.rogue.brainrottracker.data.remote

import com.rogue.brainrottracker.BuildConfig
import com.rogue.brainrottracker.data.preferences.UserSettings
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object NetworkClient {
    const val BASE_URL = "https://brainrot-server-ykrt.onrender.com/api/v1/"

    // lateinit — initialised once from MainActivity before any coroutine uses the client
    private lateinit var userSettings: UserSettings

    /** Call once at app startup (e.g. in MainActivity.onCreate) before any network request. */
    fun init(settings: UserSettings) {
        userSettings = settings
    }

    val client: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
            }
            // Automatically attach the JWT Bearer token to every outgoing request
            install(DefaultRequest) {
                val token = runBlocking {
                    if (::userSettings.isInitialized) userSettings.jwtToken.firstOrNull() else null
                }
                if (token != null) {
                    headers.append("Authorization", "Bearer $token")
                }
            }
        }
    }
}
