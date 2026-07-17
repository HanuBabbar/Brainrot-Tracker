package com.example.brainrottracker.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainrottracker.data.model.FriendProfile
import com.example.brainrottracker.data.model.FriendRequest
import com.example.brainrottracker.data.model.UserSearchResult
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.remote.FriendsApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FriendsUiState(
    val friends: List<FriendProfile> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val searchResult: UserSearchResult? = null,
    val searchError: String? = null,
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val actionMessage: String? = null,
)

class FriendsViewModel(private val userSettings: UserSettings) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    val friendCode: StateFlow<String?> = userSettings.friendCode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val userName: StateFlow<String?> = userSettings.userName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            val userId = userSettings.userId.first() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            FriendsApiService.getFriends(userId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        friends = response.friends,
                        pendingRequests = response.pendingRequests,
                        sentRequests = response.sentRequests,
                        isLoading = false,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    fun searchByCode(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null, searchResult = null)
            FriendsApiService.searchByCode(code).fold(
                onSuccess = { result ->
                    _uiState.value = _uiState.value.copy(searchResult = result, isSearching = false)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        searchError = "No user found with that code",
                        isSearching = false,
                    )
                }
            )
        }
    }

    fun sendRequest(toUserId: String) {
        viewModelScope.launch {
            val fromUserId = userSettings.userId.first() ?: return@launch
            FriendsApiService.sendRequest(fromUserId, toUserId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        searchResult = null,
                        actionMessage = "Friend request sent!",
                    )
                    loadFriends()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(actionMessage = e.message)
                }
            )
        }
    }

    fun acceptRequest(friendUserId: String) {
        viewModelScope.launch {
            val userId = userSettings.userId.first() ?: return@launch
            FriendsApiService.acceptRequest(userId, friendUserId).onSuccess { loadFriends() }
        }
    }

    fun declineRequest(friendUserId: String) {
        viewModelScope.launch {
            val userId = userSettings.userId.first() ?: return@launch
            FriendsApiService.declineRequest(userId, friendUserId).onSuccess { loadFriends() }
        }
    }

    fun removeFriend(friendUserId: String) {
        viewModelScope.launch {
            val userId = userSettings.userId.first() ?: return@launch
            FriendsApiService.removeFriend(userId, friendUserId).onSuccess { loadFriends() }
        }
    }

    fun clearSearchResult() {
        _uiState.value = _uiState.value.copy(searchResult = null, searchError = null)
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }
}
