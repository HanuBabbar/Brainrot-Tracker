package com.rogue.brainrottracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.brainrottracker.data.local.UsageEntity
import com.rogue.brainrottracker.data.repository.UsageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class SessionStats(
    val averageMinutes: Int,
    val longestMinutes: Int,
    val sessionCount: Int,
)

class WeeklyUsageViewModel(repository: UsageRepository) : ViewModel() {

    val weeklyUsage: StateFlow<List<UsageEntity>> = repository.getWeekly()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todaySessionStats: StateFlow<SessionStats> = run {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStart = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val dayEnd = cal.timeInMillis

        repository.getSessionsForDate(dayStart, dayEnd).map { sessions ->
            val durationsMs = sessions.mapNotNull { s -> s.endTime?.let { it - s.startTime } }
            SessionStats(
                averageMinutes = if (durationsMs.isNotEmpty()) (durationsMs.average() / 60_000.0).toInt() else 0,
                longestMinutes = if (durationsMs.isNotEmpty()) (durationsMs.max() / 60_000L).toInt() else 0,
                sessionCount = sessions.size,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionStats(0, 0, 0))
}
