package com.example.brainrottracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.brainrottracker.ui.friends.FriendsScreen
import com.example.brainrottracker.ui.friends.FriendsViewModel
import com.example.brainrottracker.ui.leaderboard.LeaderboardScreen
import com.example.brainrottracker.ui.leaderboard.LeaderboardViewModel
import com.example.brainrottracker.ui.login.LoginScreen
import com.example.brainrottracker.ui.login.LoginViewModel
import com.example.brainrottracker.ui.settings.SettingsScreen
import com.example.brainrottracker.ui.settings.SettingsViewModel
import com.example.brainrottracker.ui.theme.BrainrotTrackerTheme
import com.example.brainrottracker.util.NotificationHelper
import com.example.brainrottracker.data.preferences.ThemeMode
import androidx.compose.foundation.isSystemInDarkTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userSettings = UserSettings(this)
        val appViewModel = AppViewModel(userSettings)

        // 1. Initialize data layer manually for now
        val database = AppDatabase.getDatabase(this)
        val notificationHelper = NotificationHelper(this)
        val repository = UsageRepository(database.usageDao(), userSettings, notificationHelper)
        val dashboardViewModel = DashboardViewModel(repository, userSettings)
        val weeklyUsageViewModel = WeeklyUsageViewModel(repository)
        val settingsViewModel = SettingsViewModel(userSettings)
        val loginViewModel = LoginViewModel(userSettings, repository)
        val friendsViewModel = FriendsViewModel(userSettings)
        val leaderboardViewModel = LeaderboardViewModel(userSettings)

        setContent {
            val themeMode by userSettings.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val isDark = when(themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            BrainrotTrackerTheme(darkTheme = isDark) {
                AppRoot(
                    appViewModel = appViewModel,
                    dashboardViewModel = dashboardViewModel,
                    weeklyUsageViewModel = weeklyUsageViewModel,
                    settingsViewModel = settingsViewModel,
                    loginViewModel = loginViewModel,
                    friendsViewModel = friendsViewModel,
                    leaderboardViewModel = leaderboardViewModel,
                )
            }
        }
    }
}

@Composable
fun AppRoot(
    appViewModel: AppViewModel,
    dashboardViewModel: DashboardViewModel,
    weeklyUsageViewModel: WeeklyUsageViewModel,
    settingsViewModel: SettingsViewModel,
    loginViewModel: LoginViewModel,
    friendsViewModel: FriendsViewModel,
    leaderboardViewModel: LeaderboardViewModel,
) {
    val context = LocalContext.current
    val authMode by appViewModel.authMode.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Request Notification Permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Log.d("BrainrotTracker", "Notification permission granted: $isGranted")
        }
    )

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
        AuthMode.UNKNOWN -> {
            if (currentScreen is Screen.Login) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateBack = { currentScreen = Screen.Dashboard },
                    onLoginSuccess = { currentScreen = Screen.Dashboard }
                )
            } else {
                AuthChoiceScreen(
                    onContinueOffline = { appViewModel.setAuthMode(AuthMode.OFFLINE) },
                    onLogIn = { currentScreen = Screen.Login }
                )
            }
        }
        AuthMode.OFFLINE, AuthMode.LOGGED_IN -> {
            if (!isPermissionGranted) {
                PermissionScreen()
            } else {
                when (currentScreen) {
                    is Screen.Dashboard -> {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            appViewModel = appViewModel,
                            onNavigateToWeekly = { currentScreen = Screen.WeeklyUsage },
                            onNavigateToSettings = { currentScreen = Screen.Settings },
                            onNavigateToLogin = { currentScreen = Screen.Login },
                            onNavigateToFriends = {
                                friendsViewModel.loadFriends()
                                currentScreen = Screen.Friends
                            },
                            onNavigateToLeaderboard = {
                                leaderboardViewModel.refresh()
                                currentScreen = Screen.Leaderboard
                            },
                        )
                    }
                    is Screen.WeeklyUsage -> {
                        WeeklyUsageScreen(
                            viewModel = weeklyUsageViewModel,
                            onNavigateBack = { currentScreen = Screen.Dashboard }
                        )
                    }
                    is Screen.Settings -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = { currentScreen = Screen.Dashboard }
                        )
                    }
                    is Screen.Login -> {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onNavigateBack = { currentScreen = Screen.Dashboard },
                            onLoginSuccess = { currentScreen = Screen.Dashboard }
                        )
                    }
                    is Screen.Friends -> {
                        FriendsScreen(
                            viewModel = friendsViewModel,
                            onNavigateBack = { currentScreen = Screen.Dashboard },
                        )
                    }
                    is Screen.Leaderboard -> {
                        LeaderboardScreen(
                            viewModel = leaderboardViewModel,
                            onNavigateBack = { currentScreen = Screen.Dashboard },
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
