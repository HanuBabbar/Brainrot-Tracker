package com.example.brainrottracker.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainrottracker.data.model.LeaderboardEntry
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.remote.LeaderboardApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class LeaderboardTab { GLOBAL, FRIENDS }

data class LeaderboardUiState(
    val tab: LeaderboardTab = LeaderboardTab.GLOBAL,
    val entries: List<LeaderboardEntry> = emptyList(),
    val myRank: Int? = null,
    val date: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class LeaderboardViewModel(private val userSettings: UserSettings) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadGlobal()
    }

    fun switchTab(tab: LeaderboardTab) {
        _uiState.value = _uiState.value.copy(tab = tab)
        when (tab) {
            LeaderboardTab.GLOBAL -> loadGlobal()
            LeaderboardTab.FRIENDS -> loadFriends()
        }
    }

    fun refresh() {
        when (_uiState.value.tab) {
            LeaderboardTab.GLOBAL -> loadGlobal()
            LeaderboardTab.FRIENDS -> loadFriends()
        }
    }

    private fun loadGlobal() {
        viewModelScope.launch {
            val userId = userSettings.userId.first()
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            LeaderboardApiService.getGlobalLeaderboard(userId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        entries = response.leaderboard,
                        myRank = response.myRank,
                        date = response.date,
                        isLoading = false,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Could not load leaderboard: ${e.message}",
                    )
                }
            )
        }
    }

    private fun loadFriends() {
        viewModelScope.launch {
            val userId = userSettings.userId.first() ?: run {
                _uiState.value = _uiState.value.copy(
                    error = "Log in to see your friends leaderboard",
                    isLoading = false,
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            LeaderboardApiService.getFriendsLeaderboard(userId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        entries = response.leaderboard,
                        myRank = response.myRank,
                        date = response.date,
                        isLoading = false,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Could not load leaderboard: ${e.message}",
                    )
                }
            )
        }
    }
}
