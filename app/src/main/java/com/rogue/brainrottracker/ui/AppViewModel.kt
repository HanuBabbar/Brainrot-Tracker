package com.rogue.brainrottracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.brainrottracker.data.preferences.AuthMode
import com.rogue.brainrottracker.data.preferences.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(private val userSettings: UserSettings) : ViewModel() {

    val authMode: StateFlow<AuthMode> = userSettings.authMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthMode.UNKNOWN
        )

    val friendCode: StateFlow<String?> = userSettings.friendCode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userName: StateFlow<String?> = userSettings.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setAuthMode(mode: AuthMode) {
        viewModelScope.launch {
            userSettings.setAuthMode(mode)
        }
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
