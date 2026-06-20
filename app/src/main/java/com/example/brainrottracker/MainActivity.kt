package com.example.brainrottracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.brainrottracker.data.local.AppDatabase
import com.example.brainrottracker.data.preferences.AuthMode
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.repository.UsageRepository
import com.example.brainrottracker.service.BrainrotTrackerService
import com.example.brainrottracker.ui.AppViewModel
import com.example.brainrottracker.ui.Screen
import com.example.brainrottracker.ui.components.PermissionScreen
import com.example.brainrottracker.ui.dashboard.DashboardScreen
import com.example.brainrottracker.ui.dashboard.DashboardViewModel
import com.example.brainrottracker.ui.dashboard.WeeklyUsageScreen
import com.example.brainrottracker.ui.dashboard.WeeklyUsageViewModel
import com.example.brainrottracker.ui.theme.BrainrotTrackerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userSettings = UserSettings(this)
        val appViewModel = AppViewModel(userSettings)

        // 1. Initialize data layer manually for now
        val database = AppDatabase.getDatabase(this)
        val repository = UsageRepository(database.usageDao())
        val dashboardViewModel = DashboardViewModel(repository)
        val weeklyUsageViewModel = WeeklyUsageViewModel(repository)

        setContent {
            BrainrotTrackerTheme {
                AppRoot(appViewModel, dashboardViewModel, weeklyUsageViewModel)
            }
        }
    }
}

@Composable
fun AppRoot(
    appViewModel: AppViewModel,
    dashboardViewModel: DashboardViewModel,
    weeklyUsageViewModel: WeeklyUsageViewModel
) {
    val context = LocalContext.current
    val authMode by appViewModel.authMode.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Live check for accessibility permission
    var isPermissionGranted by remember {
        mutableStateOf(BrainrotTrackerService.isServiceEnabled(context))
    }

    // Observe lifecycle to re-check permission when user returns from settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = BrainrotTrackerService.isServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (authMode) {
        AuthMode.UNKNOWN -> AuthChoiceScreen(
            onContinueOffline = { appViewModel.setAuthMode(AuthMode.OFFLINE) },
            onLogIn = { /* Coming Soon */ }
        )
        AuthMode.OFFLINE, AuthMode.LOGGED_IN -> {
            if (!isPermissionGranted) {
                PermissionScreen()
            } else {
                when (currentScreen) {
                    is Screen.Dashboard -> {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            appViewModel = appViewModel,
                            onNavigateToWeekly = { currentScreen = Screen.WeeklyUsage }
                        )
                    }
                    is Screen.WeeklyUsage -> {
                        WeeklyUsageScreen(
                            viewModel = weeklyUsageViewModel,
                            onNavigateBack = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthChoiceScreen(onContinueOffline: () -> Unit, onLogIn: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Brainrot Tracker",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Stop swiping, start living.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            Button(
                onClick = onContinueOffline,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Locally (No Login)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log In (Sync Data)")
            }
        }
    }
}
