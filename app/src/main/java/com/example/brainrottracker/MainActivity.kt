package com.example.brainrottracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.brainrottracker.ui.theme.BrainrotTrackerTheme

import com.example.brainrottracker.data.local.AppDatabase
import com.example.brainrottracker.data.repository.UsageRepository
import com.example.brainrottracker.ui.dashboard.DashboardScreen
import com.example.brainrottracker.ui.dashboard.DashboardViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize data layer manually for now
        val database = AppDatabase.getDatabase(this)
        val repository = UsageRepository(database.usageDao())
        val viewModel = DashboardViewModel(repository)

        setContent {
            BrainrotTrackerTheme {
                // 2. Show the Dashboard
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}