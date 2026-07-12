package com.example.brainrottracker.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.data.preferences.AuthMode
import com.example.brainrottracker.ui.AppViewModel
import kotlinx.coroutines.launch

// ── Platform metadata ──────────────────────────────────────────────────────────

private data class PlatformMeta(
    val key: String,
    val label: String,
    val emoji: String,
    val color: Color
)

private val PLATFORMS = listOf(
    PlatformMeta("Instagram", "Instagram Reels", "📸", InstagramColor),
    PlatformMeta("YouTube",   "YouTube Shorts",  "▶",  YouTubeColor),
    PlatformMeta("TikTok",   "TikTok",           "🎵", TikTokColor)
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onMenuClick: () -> Unit,
) {
    val stats         by viewModel.todayStats.collectAsState()
    val yesterday     by viewModel.yesterdayStats.collectAsState()
    val dailyLimit    by viewModel.dailyLimit.collectAsState()

    val totalToday = stats.values.sumOf { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Brainrot Tracker") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Sidebar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Hero ring ──────────────────────────────────────────────
            DailyProgressHero(total = totalToday, limit = dailyLimit)

            // ── Platform cards ─────────────────────────────────────────
            Text(
                text = "Today's Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Instagram + YouTube side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PLATFORMS.take(2).forEach { meta ->
                    PlatformCard(
                        meta      = meta,
                        count     = stats[meta.key] ?: 0,
                        yesterday = yesterday[meta.key] ?: 0,
                        limit     = dailyLimit,
                        modifier  = Modifier.weight(1f)
                    )
                }
            }

            // TikTok full-width
            PLATFORMS.drop(2).forEach { meta ->
                PlatformCard(
                    meta      = meta,
                    count     = stats[meta.key] ?: 0,
                    yesterday = yesterday[meta.key] ?: 0,
                    limit     = dailyLimit,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            // ── Contextual message ─────────────────────────────────────
            ContextualMessage(total = totalToday, limit = dailyLimit)

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Hero: animated circular progress ring ─────────────────────────────────────

@Composable
private fun DailyProgressHero(total: Int, limit: Int) {
    val fraction = (total.toFloat() / limit.toFloat()).coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "ring"
    )
    val animatedCount by animateIntAsState(
        targetValue = total,
        animationSpec = tween(durationMillis = 900),
        label = "count"
    )

    val ringColor1 = MaterialTheme.colorScheme.primary
    val ringColor2 = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val statusText = when {
        fraction == 0f        -> "Start tracking 👀"
        fraction < 0.4f       -> "Looking good 🌿"
        fraction < 0.7f       -> "Getting there 🔥"
        fraction < 1f         -> "Almost at limit ⚠️"
        else                  -> "Limit reached 🚨"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokeWidth = 18.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                // Track arc
                drawArc(
                    color      = trackColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                // Progress arc
                if (animatedFraction > 0f) {
                    drawArc(
                        brush      = Brush.sweepGradient(
                            listOf(ringColor1, ringColor2, ringColor1),
                            center = Offset(size.width / 2f, size.height / 2f)
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * animatedFraction,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "$animatedCount",
                    style      = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text  = "of $limit today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Status pill
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text     = statusText,
                style    = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

// ── Individual platform card ──────────────────────────────────────────────────

@Composable
private fun PlatformCard(
    meta: PlatformMeta,
    count: Int,
    yesterday: Int,
    limit: Int,
    modifier: Modifier = Modifier
) {
    val progress  = (count.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
    val animProg  by animateFloatAsState(progress, tween(900), label = "bar_${meta.key}")
    val animCount by animateIntAsState(count, tween(700), label = "count_${meta.key}")

    val delta = count - yesterday
    val deltaText  = when {
        delta > 0  -> "+$delta vs yesterday"
        delta < 0  -> "$delta vs yesterday"
        else       -> "same as yesterday"
    }
    val deltaColor = when {
        delta > 0  -> Color(0xFFFF6B6B)  // more scrolling → red warning
        delta < 0  -> Color(0xFF6BCB77)  // less scrolling → green
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier  = modifier,
        shape     = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Emoji + label row
            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.Center
            ) {
                Text(meta.emoji, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = meta.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Big count — centered, aligned
            Text(
                text       = "$animCount",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth(),
                color      = meta.color
            )

            // Progress bar toward daily limit
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LinearProgressIndicator(
                    progress           = { animProg },
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color              = meta.color,
                    trackColor         = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text  = "${(animProg * 100).toInt()}% of limit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Delta badge
            Text(
                text  = deltaText,
                style = MaterialTheme.typography.labelSmall,
                color = deltaColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Contextual roast / motivational message ───────────────────────────────────

@Composable
private fun ContextualMessage(total: Int, limit: Int) {
    val fraction = total.toFloat() / limit.toFloat()
    val message = when {
        total == 0      -> "No scrolling yet today. Enjoy the silence 🧘"
        fraction < 0.25 -> "Only $total swipes so far. You're doing great 🌱"
        fraction < 0.5  -> "$total swipes. Still in the safe zone, but watch out 👀"
        fraction < 0.75 -> "Halfway through your limit. Maybe go touch some grass? 🌿"
        fraction < 1f   -> "Almost at $limit swipes. Time to put the phone down 📵"
        else            -> "Limit smashed ($total swipes). Tomorrow is a new day 💪"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        color    = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(16.dp)
        )
    }
}

// ── Sidebar header ────────────────────────────────────────────────────────────

@Composable
fun SidebarHeader(
    authMode: AuthMode,
    friendCode: String?,
    userName: String?,
    onLoginClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        if (authMode == AuthMode.LOGGED_IN) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    color    = MaterialTheme.colorScheme.primary
                ) {}
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(userName ?: "User", style = MaterialTheme.typography.titleLarge)
                    if (!friendCode.isNullOrEmpty()) {
                        Text(
                            friendCode,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
                Text("Log In for Cloud Sync")
            }
        }
    }
}
