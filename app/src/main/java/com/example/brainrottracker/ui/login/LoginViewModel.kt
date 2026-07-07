package com.example.brainrottracker.ui.login

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainrottracker.data.preferences.AuthMode
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.remote.NetworkClient
import com.example.brainrottracker.data.repository.UsageRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class LoginViewModel(
    private val userSettings: UserSettings,
    private val usageRepository: UsageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            val credentialManager = CredentialManager.create(context)
            
            // This is your Web Client ID from Google Cloud Console
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("364383832109-mdhbmh2o7ehu6akht2s46vp74vb1trtc.apps.googleusercontent.com")
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(context, request)
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("LoginViewModel", "Login failed", e)
                _uiState.value = LoginUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        
        val idToken = when {
            credential is GoogleIdTokenCredential -> {
                credential.idToken
            }
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                try {
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }

        if (idToken != null) {
            Log.d("LoginViewModel", "✅ SUCCESS! Got ID Token. Sending to server...")
            
            try {
                // Use the BASE_URL from NetworkClient
                val url = "${NetworkClient.BASE_URL}auth/google"
                val response = NetworkClient.client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("idToken" to idToken))
                }
                
                if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                    Log.d("LoginViewModel", "✅ Server Auth Successful!")

                    // Parse server response to get userId + friendCode
                    val authResponse = try {
                        response.body<AuthResponse>()
                    } catch (e: Exception) {
                        null
                    }

                    val userId = authResponse?.user?.userId
                        ?: try {
                            GoogleIdTokenCredential.createFrom(credential.data).id
                        } catch (e: Exception) {
                            "google_user_${System.currentTimeMillis()}"
                        }

                    userSettings.setUserId(userId)
                    authResponse?.user?.friendCode?.let { userSettings.setFriendCode(it) }
                    userSettings.setAuthMode(AuthMode.LOGGED_IN)

                    // Immediately sync all local data under the correct server userId
                    // so the leaderboard shows accurate counts right away
                    try {
                        usageRepository.syncData()
                        Log.d("LoginViewModel", "✅ Post-login sync complete")
                    } catch (e: Exception) {
                        Log.w("LoginViewModel", "Post-login sync failed (non-fatal)", e)
                    }

                    _uiState.value = LoginUiState.Success
                } else {
                    Log.e("LoginViewModel", "❌ Server Auth Failed: ${response.status}")
                    _uiState.value = LoginUiState.Error("Server rejected login: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ Network Error during Auth", e)
                _uiState.value = LoginUiState.Error("Could not connect to server: ${e.message}")
            }
        } else {
            Log.e("LoginViewModel", "❌ Failed to extract token. Type: ${credential.type}")
            _uiState.value = LoginUiState.Error("Unexpected credential type: ${credential.type}")
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

// Response models for auth
@Serializable
data class AuthUserInfo(
    val userId: String,
    val name: String,
    val emailId: String,
    val friendCode: String? = null,
)

@Serializable
data class AuthResponse(
    val message: String,
    val user: AuthUserInfo,
)

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
