package com.example.brainrottracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brainrottracker.data.preferences.AuthMode
import com.example.brainrottracker.ui.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    appViewModel: AppViewModel,
    onNavigateToWeekly: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val stats by viewModel.todayStats.collectAsState()
    val authMode by appViewModel.authMode.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SidebarHeader(authMode = authMode)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Weekly Usage") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToWeekly()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Brainrot Tracker") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Sidebar")
                        }
                    }
                )
            }
        ) { padding ->
            val instagramCount = stats["Instagram"] ?: 0
            val youtubeCount = stats["YouTube"] ?: 0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = "Today's Consumption",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                StatCard(
                    platform = "Instagram Reels",
                    count = instagramCount,
                    color = MaterialTheme.colorScheme.primaryContainer
                )

                StatCard(
                    platform = "YouTube Shorts",
                    count = youtubeCount,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )

                Text(
                    text = "Total Today: ${instagramCount + youtubeCount}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun SidebarHeader(authMode: AuthMode) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        if (authMode == AuthMode.LOGGED_IN) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primary
                ) {}
                Spacer(modifier = Modifier.width(16.dp))
                Text("Hanu", style = MaterialTheme.typography.titleLarge)
            }
        } else {
            Button(
                onClick = { /* Navigate to Login later */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log In for Cloud Sync")
            }
        }
    }
}

@Composable
fun StatCard(platform: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = platform, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "$count",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black
            )
        }
    }
}
