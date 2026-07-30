package com.rogue.brainrottracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.brainrottracker.data.preferences.CPUMode
import com.rogue.brainrottracker.data.preferences.ThemeMode
import com.rogue.brainrottracker.data.preferences.AuthMode
import com.rogue.brainrottracker.data.preferences.UserSettings
import com.rogue.brainrottracker.data.remote.ProfileApiService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userSettings: UserSettings
) : ViewModel() {

    private val _updateNameState = MutableStateFlow<UiState>(UiState.Idle)
    val updateNameState = _updateNameState.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    val userId: StateFlow<String?> = userSettings.userId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val authMode: StateFlow<AuthMode> = userSettings.authMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthMode.UNKNOWN)

    val userName: StateFlow<String?> = userSettings.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val friendCode: StateFlow<String?> = userSettings.friendCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyLimit: StateFlow<Int> = userSettings.dailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    val cpuMode: StateFlow<CPUMode> = userSettings.cpuMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CPUMode.MEDIUM)

    val vibrationEnabled: StateFlow<Boolean> = userSettings.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val persistentNotificationEnabled: StateFlow<Boolean> = userSettings.persistentNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val strictModeEnabled: StateFlow<Boolean> = userSettings.strictModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<ThemeMode> = userSettings.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun setDailyLimit(limit: Int) {
        viewModelScope.launch {
            userSettings.setDailyLimit(limit)
        }
    }

    fun setCPUMode(mode: CPUMode) {
        viewModelScope.launch {
            userSettings.setCPUMode(mode)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setVibrationEnabled(enabled)
        }
    }

    fun setPersistentNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setPersistentNotificationEnabled(enabled)
        }
    }

    fun setStrictModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setStrictModeEnabled(enabled)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userSettings.setThemeMode(mode)
        }
    }

    fun updateUserName(newName: String) {
        if (newName.isBlank()) {
            _updateNameState.value = UiState.Error("Name cannot be empty")
            return
        }

        viewModelScope.launch {
            val currentUserId = userSettings.userId.firstOrNull()
            if (currentUserId == null) {
                _updateNameState.value = UiState.Error("User ID not found. Try logging in again.")
                return@launch
            }

            _updateNameState.value = UiState.Loading
            val result = ProfileApiService.updateProfileName(currentUserId, newName)
            
            result.onSuccess { response ->
                userSettings.setUserName(newName.trim())
                _updateNameState.value = UiState.Success("Profile updated successfully")
            }.onFailure { error ->
                _updateNameState.value = UiState.Error(error.message ?: "Failed to update profile")
            }
        }
    }
    
    fun resetUpdateNameState() {
        _updateNameState.value = UiState.Idle
    }
    
    fun logout() {
        viewModelScope.launch {
            userSettings.setUserId(null)
            userSettings.setFriendCode(null)
            userSettings.setUserName(null)
            userSettings.setAuthMode(AuthMode.UNKNOWN)
        }
    }
}
