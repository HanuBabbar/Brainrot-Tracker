package com.example.brainrottracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainrottracker.data.preferences.AuthMode
import com.example.brainrottracker.data.preferences.UserSettings
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

    fun setAuthMode(mode: AuthMode) {
        viewModelScope.launch {
            userSettings.setAuthMode(mode)
        }
    }
}
