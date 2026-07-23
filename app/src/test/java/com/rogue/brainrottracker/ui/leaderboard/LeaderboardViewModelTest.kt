package com.rogue.brainrottracker.ui.leaderboard

import com.rogue.brainrottracker.data.model.LeaderboardEntry
import com.rogue.brainrottracker.data.model.LeaderboardResponse
import com.rogue.brainrottracker.data.preferences.UserSettings
import com.rogue.brainrottracker.data.remote.LeaderboardApiService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    private lateinit var userSettings: UserSettings
    private lateinit var viewModel: LeaderboardViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userSettings = mockk(relaxed = true)
        mockkObject(LeaderboardApiService)
        
        every { userSettings.userId } returns flowOf("test_user_id")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initialization loads global leaderboard`() = runTest {
        val mockResponse = LeaderboardResponse(
            date = "2024-01-01",
            leaderboard = listOf(LeaderboardEntry(1, "test_user_id", "Test User", 50, true)),
            myRank = 1
        )
        coEvery { LeaderboardApiService.getGlobalLeaderboard(any()) } returns Result.success(mockResponse)
        
        viewModel = LeaderboardViewModel(userSettings)
        
        // Advance dispatcher to execute the init block's coroutine
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(LeaderboardTab.GLOBAL, state.tab)
        assertEquals(false, state.isLoading)
        assertEquals(1, state.entries.size)
        assertEquals(1, state.myRank)
        assertEquals(null, state.error)
    }

    @Test
    fun `switch to friends tab loads friends leaderboard`() = runTest {
        // First mock global for init
        coEvery { LeaderboardApiService.getGlobalLeaderboard(any()) } returns Result.success(
            LeaderboardResponse("date", emptyList(), null)
        )
        
        viewModel = LeaderboardViewModel(userSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        // Now mock friends API
        val mockFriendsResponse = LeaderboardResponse(
            date = "2024-01-01",
            leaderboard = listOf(LeaderboardEntry(1, "friend_id", "Friend", 100, false)),
            myRank = 2
        )
        coEvery { LeaderboardApiService.getFriendsLeaderboard("test_user_id") } returns Result.success(mockFriendsResponse)

        viewModel.switchTab(LeaderboardTab.FRIENDS)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LeaderboardTab.FRIENDS, state.tab)
        assertEquals(1, state.entries.size)
        assertEquals("Friend", state.entries[0].name)
    }

    @Test
    fun `error during load updates error state`() = runTest {
        coEvery { LeaderboardApiService.getGlobalLeaderboard(any()) } returns Result.failure(Exception("Network error"))
        
        viewModel = LeaderboardViewModel(userSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assert(state.error?.contains("Network error") == true)
    }
}
