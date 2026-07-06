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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userSettings: UserSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            val credentialManager = CredentialManager.create(context)
            
            // TODO: Replace with your actual Web Client ID from Google Cloud Console
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("364383832109-b7uhk4037c1042u93bcsp3ptfpk8qjjd.apps.googleusercontent.com")
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
        
        if (credential is GoogleIdTokenCredential) {
            val idToken = credential.idToken
            Log.d("LoginViewModel", "Got ID Token: $idToken")
            
            userSettings.setUserId(credential.id)
            userSettings.setAuthMode(AuthMode.LOGGED_IN)
            _uiState.value = LoginUiState.Success
        } else {
            _uiState.value = LoginUiState.Error("Unexpected credential type")
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
