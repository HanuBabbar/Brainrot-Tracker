package com.example.brainrottracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainrottracker.data.local.UsageEntity
import com.example.brainrottracker.data.preferences.AuthMode
import com.example.brainrottracker.data.preferences.CPUMode
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.repository.UsageRepository
import com.example.brainrottracker.util.getLongestSessionMs
import com.example.brainrottracker.util.getTotalTimeWastedMs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class AppViewModel(
    application: Application,
    private val userSettings: UserSettings,
    private val repository: UsageRepository
) : AndroidViewModel(application) {

    private val _timeWastedMs = MutableStateFlow(0L)
    val timeWastedMs: StateFlow<Long> = _timeWastedMs.asStateFlow()

    private val _longestSessionMs = MutableStateFlow(0L)
    val longestSessionMs: StateFlow<Long> = _longestSessionMs.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                refreshUsageStats()
                delay(10000) // refresh every 10 seconds
            }
        }
    }

    fun refreshUsageStats() {
        val context = getApplication<Application>()
        _timeWastedMs.value = getTotalTimeWastedMs(context)
        _longestSessionMs.value = getLongestSessionMs(context)
    }

    val cpuMode: StateFlow<CPUMode> = userSettings.cpuMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CPUMode.MEDIUM
        )

    val authMode: StateFlow<AuthMode> = userSettings.authMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthMode.UNKNOWN
        )

    val dailyLimit: StateFlow<Int> = userSettings.dailyLimit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 200
        )

    val breakReminder: StateFlow<Int> = userSettings.breakReminder
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 20
        )

    val darkMode: StateFlow<Boolean> = userSettings.darkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val streaks: StateFlow<Pair<Int, Int>> = combine(
        repository.getAll(),
        userSettings.dailyLimit
    ) { list, limit ->
        calculateStreaks(list, limit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(0, 0)
    )

    val currentStreak: StateFlow<Int> = streaks
        .map { it.first }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val bestStreak: StateFlow<Int> = streaks
        .map { it.second }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private fun calculateStreaks(list: List<UsageEntity>, limit: Int): Pair<Int, Int> {
        if (list.isEmpty()) return Pair(0, 0)
        
        val dailyTotals = list.groupBy { it.date }.mapValues { (_, entries) -> entries.sumOf { it.count } }
        val dates = dailyTotals.keys.sorted()
        val earliestDateStr = dates.first()
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val earliestDate = try { sdf.parse(earliestDateStr) } catch(e: Exception) { null } ?: return Pair(0, 0)
        
        val calendar = Calendar.getInstance()
        val todayStr = sdf.format(calendar.time)
        
        val checkCal = Calendar.getInstance().apply { time = earliestDate }
        
        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0
        
        while (true) {
            val dateStr = sdf.format(checkCal.time)
            val totalOnDate = dailyTotals[dateStr] ?: 0
            
            if (totalOnDate <= limit) {
                tempStreak++
                if (tempStreak > bestStreak) {
                    bestStreak = tempStreak
                }
            } else {
                tempStreak = 0
            }
            
            if (dateStr == todayStr) {
                currentStreak = tempStreak
                break
            }
            
            if (checkCal.timeInMillis > System.currentTimeMillis()) {
                break
            }
            checkCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return Pair(currentStreak, bestStreak)
    }

    fun setAuthMode(mode: AuthMode) {
        viewModelScope.launch {
            userSettings.setAuthMode(mode)
        }
    }

    fun setDailyLimit(limit: Int) {
        viewModelScope.launch {
            userSettings.setDailyLimit(limit)
        }
    }

    fun setBreakReminder(minutes: Int) {
        viewModelScope.launch {
            userSettings.setBreakReminder(minutes)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setDarkMode(enabled)
        }
    }

    fun setCPUMode(mode: CPUMode) {
        viewModelScope.launch {
            userSettings.setCPUMode(mode)
        }
    }
}
