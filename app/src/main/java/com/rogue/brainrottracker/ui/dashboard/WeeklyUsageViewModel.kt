package com.rogue.brainrottracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.brainrottracker.data.local.UsageEntity
import com.rogue.brainrottracker.data.repository.UsageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WeeklyUsageViewModel(repository: UsageRepository) : ViewModel() {

    val weeklyUsage: StateFlow<List<UsageEntity>> = repository.getWeekly()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
