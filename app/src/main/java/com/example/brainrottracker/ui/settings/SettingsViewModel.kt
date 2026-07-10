package com.example.brainrottracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainrottracker.data.preferences.CPUMode
import com.example.brainrottracker.data.preferences.ThemeMode
import com.example.brainrottracker.data.preferences.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userSettings: UserSettings
) : ViewModel() {

    val dailyLimit: StateFlow<Int> = userSettings.dailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    val cpuMode: StateFlow<CPUMode> = userSettings.cpuMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CPUMode.MEDIUM)

    val vibrationEnabled: StateFlow<Boolean> = userSettings.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val persistentNotificationEnabled: StateFlow<Boolean> = userSettings.persistentNotificationEnabled
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

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userSettings.setThemeMode(mode)
        }
    }
}
