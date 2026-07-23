package com.rogue.brainrottracker.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import com.rogue.brainrottracker.data.preferences.AuthMode
import com.rogue.brainrottracker.ui.AppViewModel
import kotlinx.coroutines.launch
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ContentCopy

import androidx.compose.ui.res.painterResource
import com.rogue.brainrottracker.R

// ── Platform metadata ──────────────────────────────────────────────────────────

private data class PlatformMeta(
    val key: String,
    val label: String,
    val iconResId: Int,
    val color: Color
)

@Composable
private fun platforms(): List<PlatformMeta> {
    val accent = MaterialTheme.colorScheme.primary
    return listOf(
        PlatformMeta("Instagram", "Instagram Reels", R.drawable.ic_instagram, accent),
        PlatformMeta("YouTube",   "YouTube Shorts",  R.drawable.ic_youtube,  accent),
        PlatformMeta("TikTok",   "TikTok",           R.drawable.ic_tiktok,   accent)
    )
}

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
    val isPerfectWeek by viewModel.isPerfectWeek.collectAsState()
    val userName      by viewModel.userName.collectAsState()
    
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val totalToday = stats.values.sumOf { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Brainrot Tracker", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
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
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500))
            ) {
                DailyProgressHero(total = totalToday, limit = dailyLimit)
            }

            if (isPerfectWeek) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔥 7-Day Perfect Streak!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            // ── Platform cards ─────────────────────────────────────────
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 100))
            ) {
                Text(
                    text = "Today's Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            val platformList = platforms()
            val sortedPlatforms = platformList.sortedByDescending { stats[it.key] ?: 0 }

            if (sortedPlatforms.isNotEmpty()) {
                val heroPlatform = sortedPlatforms.first()
                val remainingPlatforms = sortedPlatforms.drop(1)

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(500, delayMillis = 200)) + fadeIn(tween(500, delayMillis = 200))
                ) {
                    PlatformCard(
                        meta      = heroPlatform,
                        count     = stats[heroPlatform.key] ?: 0,
                        yesterday = yesterday[heroPlatform.key] ?: 0,
                        limit     = dailyLimit,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }

                if (remainingPlatforms.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(500, delayMillis = 300)) + fadeIn(tween(500, delayMillis = 300))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            remainingPlatforms.forEach { meta ->
                                PlatformCard(
                                    meta      = meta,
                                    count     = stats[meta.key] ?: 0,
                                    yesterday = yesterday[meta.key] ?: 0,
                                    limit     = dailyLimit,
                                    modifier  = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Contextual message ─────────────────────────────────────
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(500, delayMillis = 400)) + fadeIn(tween(500, delayMillis = 400))
            ) {
                ContextualMessage(total = totalToday, limit = dailyLimit)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Hero: animated circular progress ring ─────────────────────────────────────

@Composable
private fun DailyProgressHero(total: Int, limit: Int) {
    val rawFraction = (total.toFloat() / limit.toFloat())
    val baseFraction = rawFraction.coerceIn(0f, 1f)
    val overFraction = (rawFraction - 1f).coerceIn(0f, 1f) // Up to 200% visually
    val isIdle = total == 0

    val animatedBaseFraction by animateFloatAsState(
        targetValue = baseFraction,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "base_ring"
    )
    val animatedOverFraction by animateFloatAsState(
        targetValue = overFraction,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "over_ring"
    )
    val animatedCount by animateIntAsState(
        targetValue = total,
        animationSpec = tween(durationMillis = 900),
        label = "count"
    )

    // Idle pulse animation — gently scales the ring up and down when no scrolls
    val pulseAnim = rememberInfiniteTransition(label = "idle_pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue  = if (isIdle) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ringColor1 = MaterialTheme.colorScheme.primary
    val ringColor2 = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val errorColor = MaterialTheme.colorScheme.error

    val statusText = when {
        rawFraction == 0f     -> "You're clean today! 🌿"
        rawFraction < 0.4f    -> "Looking good 🌿"
        rawFraction < 0.7f    -> "Getting there 🔥"
        rawFraction < 1f      -> "Almost at limit ⚠️"
        else                  -> "Limit smashed 🚨"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier
                .size(180.dp)
                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
            ) {
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
                if (animatedBaseFraction > 0f) {
                    drawArc(
                        brush      = Brush.sweepGradient(
                            listOf(ringColor1, ringColor2, ringColor1),
                            center = Offset(size.width / 2f, size.height / 2f)
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * animatedBaseFraction,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Over-limit arc
                if (animatedOverFraction > 0f) {
                    drawArc(
                        color      = errorColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedOverFraction,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = if (isIdle) "🌿" else "$animatedCount",
                    style      = if (isIdle) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text  = if (isIdle) "Ready to track" else "of $limit today",
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
    val rawFraction = if (limit > 0) (count.toFloat() / limit.toFloat()) else 0f
    val baseFraction = rawFraction.coerceIn(0f, 1f)
    val overFraction = (rawFraction - 1f).coerceIn(0f, 1f)

    val animBaseProg  by animateFloatAsState(baseFraction, tween(900), label = "bar_${meta.key}")
    val animOverProg  by animateFloatAsState(overFraction, tween(900), label = "over_${meta.key}")
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, tween(150), label = "scale")
    val haptic = LocalHapticFeedback.current

    Card(
        modifier  = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
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
                Icon(
                    painter = painterResource(id = meta.iconResId),
                    contentDescription = meta.label,
                    modifier = Modifier.size(16.dp),
                    tint = meta.color
                )
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
                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth().height(4.dp)) {
                    LinearProgressIndicator(
                        progress           = { animBaseProg },
                        modifier           = Modifier.fillMaxSize(),
                        color              = meta.color,
                        trackColor         = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    if (animOverProg > 0f) {
                        LinearProgressIndicator(
                            progress           = { animOverProg },
                            modifier           = Modifier.fillMaxSize(),
                            color              = MaterialTheme.colorScheme.error,
                            trackColor         = Color.Transparent,
                        )
                    }
                }
                val percentString = if (rawFraction > 1f) "${(rawFraction * 100).toInt()}% (+${((rawFraction - 1f) * 100).toInt()}%)" else "${(animBaseProg * 100).toInt()}% of limit"
                Text(
                    text  = percentString,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (rawFraction > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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
