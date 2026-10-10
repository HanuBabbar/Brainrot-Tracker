package com.rogue.brainrottracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rogue.brainrottracker.data.preferences.UserSettings
import com.rogue.brainrottracker.data.repository.UsageRepository
import com.rogue.brainrottracker.ui.dashboard.DashboardViewModel
import com.rogue.brainrottracker.ui.dashboard.WeeklyUsageViewModel
import com.rogue.brainrottracker.ui.friends.FriendsViewModel
import com.rogue.brainrottracker.ui.leaderboard.LeaderboardViewModel
import com.rogue.brainrottracker.ui.login.LoginViewModel
import com.rogue.brainrottracker.ui.settings.SettingsViewModel

class AppViewModelFactory(
    private val userSettings: UserSettings,
    private val repository: UsageRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AppViewModel::class.java -> AppViewModel(userSettings) as T
            DashboardViewModel::class.java -> DashboardViewModel(repository, userSettings) as T
            WeeklyUsageViewModel::class.java -> WeeklyUsageViewModel(repository) as T
            SettingsViewModel::class.java -> SettingsViewModel(userSettings) as T
            LoginViewModel::class.java -> LoginViewModel(userSettings, repository) as T
            FriendsViewModel::class.java -> FriendsViewModel(userSettings) as T
            LeaderboardViewModel::class.java -> LeaderboardViewModel(userSettings) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
