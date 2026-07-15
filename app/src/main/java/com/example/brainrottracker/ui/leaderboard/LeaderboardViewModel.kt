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
    val globalEntries: List<LeaderboardEntry> = emptyList(),
    val friendsEntries: List<LeaderboardEntry> = emptyList(),
    val globalMyRank: Int? = null,
    val friendsMyRank: Int? = null,
    val date: String = "",
    val isLoadingGlobal: Boolean = false,
    val isLoadingFriends: Boolean = false,
    val errorGlobal: String? = null,
    val errorFriends: String? = null,
)

class LeaderboardViewModel(private val userSettings: UserSettings) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private var lastGlobalRefreshTime = 0L
    private var lastFriendsRefreshTime = 0L

    init {
        loadGlobal()
        loadFriends()
    }

    fun refresh() {
        lastGlobalRefreshTime = 0L
        lastFriendsRefreshTime = 0L
        loadGlobal()
        loadFriends()
    }

    fun onTabSelected(tab: LeaderboardTab) {
        val now = System.currentTimeMillis()
        when (tab) {
            LeaderboardTab.GLOBAL -> {
                if (now - lastGlobalRefreshTime > 10_000) {
                    loadGlobal()
                }
            }
            LeaderboardTab.FRIENDS -> {
                if (now - lastFriendsRefreshTime > 10_000) {
                    loadFriends()
                }
            }
        }
    }

    private fun loadGlobal() {
        lastGlobalRefreshTime = System.currentTimeMillis()
        viewModelScope.launch {
            val userId = userSettings.userId.first()
            _uiState.value = _uiState.value.copy(isLoadingGlobal = true, errorGlobal = null)
            LeaderboardApiService.getGlobalLeaderboard(userId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        globalEntries = response.leaderboard,
                        globalMyRank = response.myRank,
                        date = response.date.ifEmpty { _uiState.value.date },
                        isLoadingGlobal = false,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingGlobal = false,
                        errorGlobal = "Could not load global leaderboard: ${e.message}",
                    )
                }
            )
        }
    }

    private fun loadFriends() {
        lastFriendsRefreshTime = System.currentTimeMillis()
        viewModelScope.launch {
            val userId = userSettings.userId.first() ?: run {
                _uiState.value = _uiState.value.copy(
                    errorFriends = "Log in to see your friends leaderboard",
                    isLoadingFriends = false,
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoadingFriends = true, errorFriends = null)
            LeaderboardApiService.getFriendsLeaderboard(userId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        friendsEntries = response.leaderboard,
                        friendsMyRank = response.myRank,
                        date = response.date.ifEmpty { _uiState.value.date },
                        isLoadingFriends = false,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingFriends = false,
                        errorFriends = "Could not load friends leaderboard: ${e.message}",
                    )
                }
            )
        }
    }
}
