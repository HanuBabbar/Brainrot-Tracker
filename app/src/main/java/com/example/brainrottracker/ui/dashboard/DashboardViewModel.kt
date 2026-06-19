package com.example.brainrottracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainrottracker.data.repository.UsageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(repository: UsageRepository) : ViewModel() {

    // Observe all weekly usage to filter specifically for today's counts per platform
    val todayStats: StateFlow<Map<String, Int>> = repository.getWeekly()
        .map { list ->
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            list.filter { it.date == todayDate }
                .associate { it.platform to it.count }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
}
