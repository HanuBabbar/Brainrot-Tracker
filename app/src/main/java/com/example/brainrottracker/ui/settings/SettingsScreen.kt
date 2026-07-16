package com.example.brainrottracker.ui.settings

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.brainrottracker.data.preferences.CPUMode
import com.example.brainrottracker.data.preferences.ThemeMode
import com.example.brainrottracker.data.preferences.AuthMode
import com.example.brainrottracker.service.BrainrotTrackerService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val dailyLimit by viewModel.dailyLimit.collectAsState()
    val cpuMode by viewModel.cpuMode.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val persistentNotificationEnabled by viewModel.persistentNotificationEnabled.collectAsState()
    val strictModeEnabled by viewModel.strictModeEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    
    val userName by viewModel.userName.collectAsState()
    val updateNameState by viewModel.updateNameState.collectAsState()
    val authMode by viewModel.authMode.collectAsState()

    val context = LocalContext.current
    var limitInput by remember(dailyLimit) { mutableStateOf(dailyLimit.toString()) }
    var nameInput by remember(userName) { mutableStateOf(userName ?: "") }

    LaunchedEffect(updateNameState) {
        when (val state = updateNameState) {
            is SettingsViewModel.UiState.Success -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateNameState()
            }
            is SettingsViewModel.UiState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateNameState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Profile / Login Section
            if (authMode == AuthMode.LOGGED_IN) {
                // Show Profile Card for logged-in users
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProfile() },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Your Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "Name, Friend Code, Log Out",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Go to Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Show Log In card for offline users
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Using Locally", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            "Log in to sync your stats, compete with friends, and access the leaderboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Log In & Sync Data")
                        }
                    }
                }
            }
            
            HorizontalDivider()

            // Daily Limit Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text(
                        text = "Daily Swiping Limit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You will be notified when you hit this number.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { input ->
                        // Only allow digits
                        val filtered = input.filter { it.isDigit() }
                        limitInput = filtered

                        val parsed = filtered.toIntOrNull()
                        if (parsed != null && parsed in 1..1000) {
                            viewModel.setDailyLimit(parsed)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Max Swipes (1-1000)") },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Strict Mode (App Blocking)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        Text(
                            "Automatically take you to the Home screen when you exceed your daily limit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = strictModeEnabled,
                        onCheckedChange = { viewModel.setStrictModeEnabled(it) }
                    )
                }
            }

            HorizontalDivider()

            // Notifications Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibrate on limit reached", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Play a haptic pattern when you exceed your daily limit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Persistent notification", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Show an ongoing notification to quickly disable the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = persistentNotificationEnabled,
                        onCheckedChange = { viewModel.setPersistentNotificationEnabled(it) }
                    )
                }
            }

            HorizontalDivider()

            // Theme Selection Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider()

            // CPU Mode Section
            Column {
                Text(
                    text = "CPU Performance Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lower modes save battery but may miss swipes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(16.dp))

                CPUMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setCPUMode(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = cpuMode == mode,
                            onClick = { viewModel.setCPUMode(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = mode.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${mode.intervalMs}ms interval",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Banking Mode / Disable Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(
                        text = "Banking Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Some banking apps block accessibility services. You can temporarily disable the tracker here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = {
                        val intent = Intent(BrainrotTrackerService.ACTION_DISABLE_SERVICE)
                        intent.setPackage(context.packageName)
                        context.sendBroadcast(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Disable Tracker")
                }

                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Note: Opening the app again will automatically prompt you to re-enable permissions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
