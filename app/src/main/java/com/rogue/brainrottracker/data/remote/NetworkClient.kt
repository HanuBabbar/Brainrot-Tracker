package com.rogue.brainrottracker.data.remote

import com.rogue.brainrottracker.BuildConfig
import com.rogue.brainrottracker.data.preferences.UserSettings
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object NetworkClient {
    const val BASE_URL = "https://brainrot-server-ykrt.onrender.com/api/v1/"

    @Volatile
    var currentJwtToken: String? = null
        private set

    /** Call once at app startup (e.g. in MainActivity.onCreate) before any network request. */
    fun init(settings: UserSettings, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        scope.launch {
            settings.jwtToken.collectLatest { token ->
                currentJwtToken = token
            }
        }
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
                val token = currentJwtToken
                if (!token.isNullOrEmpty()) {
                    headers.append("Authorization", "Bearer $token")
                }
            }
        }
    }
}
