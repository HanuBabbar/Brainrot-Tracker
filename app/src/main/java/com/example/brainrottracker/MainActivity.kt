package com.example.brainrottracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.brainrottracker.data.local.AppDatabase
import com.example.brainrottracker.data.preferences.AuthMode
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.repository.UsageRepository
import com.example.brainrottracker.service.BrainrotTrackerService
import com.example.brainrottracker.ui.AppViewModel
import com.example.brainrottracker.ui.MainShell
import com.example.brainrottracker.ui.components.GradientButton
import com.example.brainrottracker.ui.components.PermissionScreen
import com.example.brainrottracker.ui.dashboard.DashboardViewModel
import com.example.brainrottracker.ui.dashboard.WeeklyUsageViewModel
import com.example.brainrottracker.ui.theme.*
import com.example.brainrottracker.util.NotificationHelper
import com.example.brainrottracker.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable immersive full-screen mode (transient hide of status & navigation bars)
        val window = this.window
        val view = window.decorView
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val userSettings = UserSettings(this)
        val database = AppDatabase.getDatabase(this)
        val notificationHelper = NotificationHelper(this)
        val repository = UsageRepository(database.usageDao(), userSettings, notificationHelper)

        val appViewModel = AppViewModel(application, userSettings, repository)
        val dashboardViewModel = DashboardViewModel(repository)
        val weeklyUsageViewModel = WeeklyUsageViewModel(repository)

        setContent {
            val darkMode by appViewModel.darkMode.collectAsState()
            BrainrotTrackerTheme(darkTheme = darkMode) {
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
                MainShell(
                    appViewModel = appViewModel,
                    dashboardViewModel = dashboardViewModel,
                    weeklyUsageViewModel = weeklyUsageViewModel
                )
            }
        }
    }
}

@Composable
fun AuthChoiceScreen(onContinueOffline: () -> Unit, onLogIn: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.brain_icon),
                contentDescription = "Brain Rot mascot",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "brain",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = ForegroundLight
                )
                Text(
                    text = "rot",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = LightPurple
                )
            }

            Text(
                text = "Stop swiping, start living.",
                fontSize = 14.sp,
                color = MutedForeground,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            GradientButton(
                text = "Use Locally (No Login)",
                onClick = onContinueOffline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderPurpleStrong, RoundedCornerShape(16.dp))
                    .clickable(onClick = onLogIn)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log In (Sync Data)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightPurple
                )
            }
        }
    }
}
