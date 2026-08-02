package com.rogue.brainrottracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.brainrottracker.data.preferences.UserSettings
import com.rogue.brainrottracker.data.repository.UsageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayTotal(
    val weekday: String,
    val dayOfMonth: String,
    val total: Int,
    val isToday: Boolean,
)

class DashboardViewModel(
    repository: UsageRepository,
    userSettings: UserSettings
) : ViewModel() {

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayDate: String get() = fmt.format(Date())
    private val yesterdayDate: String get() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return fmt.format(cal.time)
    }

    /** Per-platform swipe counts for today, e.g. {"Instagram" to 14, "YouTube" to 3}. */
    val todayStats: StateFlow<Map<String, Int>> = repository.getWeekly()
        .map { list ->
            list.filter { it.date == todayDate }
                .associate { it.platform to it.count }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Per-platform swipe counts for yesterday — used to compute deltas. */
    val yesterdayStats: StateFlow<Map<String, Int>> = repository.getWeekly()
        .map { list ->
            list.filter { it.date == yesterdayDate }
                .associate { it.platform to it.count }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** The user's configured daily swipe limit. */
    val dailyLimit: StateFlow<Int> = userSettings.dailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    val isPerfectWeek: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(repository.getWeekly(), userSettings.dailyLimit) { list, limit ->
        // Group rows by date and sum per-day totals
        val dailyTotals = list.groupBy { it.date }
            .mapValues { (_, entries) -> entries.sumOf { it.count } }

        // Require data for all 7 days AND every day must be under the limit
        if (dailyTotals.size < 7) return@combine false
        val breached = dailyTotals.any { (_, total) -> total >= limit }
        !breached
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Per-day totals for the last 7 calendar days (oldest→newest), zero-filled for missing days. */
    val weeklyDailyTotals: StateFlow<List<DayTotal>> = repository.getWeekly()
        .map { list ->
            val byDate = list.groupBy { it.date }.mapValues { (_, e) -> e.sumOf { it.count } }
            val labelFmt = SimpleDateFormat("EEE", Locale.getDefault())
            val dayFmt = SimpleDateFormat("d", Locale.getDefault())
            (6 downTo 0).map { back ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -back)
                val key = fmt.format(cal.time)
                DayTotal(
                    weekday = labelFmt.format(cal.time),
                    dayOfMonth = dayFmt.format(cal.time),
                    total = byDate[key] ?: 0,
                    isToday = back == 0,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userName: StateFlow<String?> = userSettings.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val friendCode: StateFlow<String?> = userSettings.friendCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Minutes elapsed in the current (still-warm) scrolling session, or null if idle. */
    val activeSessionMinutes: StateFlow<Int?> = repository.observeActiveSessionMinutes()
        .map { session ->
            session?.let {
                val durationMs = (it.endTime ?: it.startTime) - it.startTime
                (durationMs / 60_000L).toInt()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
