package com.example.brainrottracker.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.data.local.UsageEntity
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.util.Calendar
import kotlinx.coroutines.delay



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyUsageScreen(
    viewModel: WeeklyUsageViewModel,
    onNavigateBack: () -> Unit
) {
    val weeklyData by viewModel.weeklyUsage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Usage") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Sidebar")
                    }
                }
            )
        }
    ) { padding ->
        if (weeklyData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No data yet.\nStart scrolling to see your stats here 👀",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                WeeklySummaryRow(data = weeklyData)
                WeeklyChartCard(data = weeklyData)
                PlatformBreakdownCard(data = weeklyData)
            }
        }
    }
}

// ─── Summary stat chips at the top ───────────────────────────────────────────

@Composable
private fun WeeklySummaryRow(data: List<UsageEntity>) {
    val last7Days = data
        .groupBy { it.date }
        .mapValues { e -> e.value.sumOf { it.count } }
        .toList()
        .sortedBy { it.first }
        .takeLast(7)

    val weekTotal   = last7Days.sumOf { it.second }
    val bestDay     = last7Days.maxByOrNull { it.second }
    val bestDayName = bestDay?.first?.let { dateLabel(it) } ?: "—"
    val bestDayVal  = bestDay?.second ?: 0

    val igTotal = data.filter { it.platform == "Instagram" }.sumOf { it.count }
    val ytTotal = data.filter { it.platform == "YouTube" }.sumOf { it.count }
    val topPlatform = if (igTotal >= ytTotal) "Instagram" else "YouTube"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryChip(
            label = "This week",
            value = "$weekTotal",
            subtitle = "swipes",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Best day",
            value = bestDayName,
            subtitle = "$bestDayVal swipes",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Most used",
            value = if (topPlatform == "Instagram") "IG" else "YT",
            subtitle = topPlatform,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Grouped bar chart card ───────────────────────────────────────────────────

@Composable
private fun WeeklyChartCard(data: List<UsageEntity>) {
    // Shared state: text to show when a bar is tapped (null = hidden)
    var selectedInfo by remember { mutableStateOf<String?>(null) }

    // Auto-dismiss after 2.5 seconds
    LaunchedEffect(selectedInfo) {
        if (selectedInfo != null) {
            delay(2500)
            selectedInfo = null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Last 7 Days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap a bar to see details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tap-reveal tooltip chip
                AnimatedVisibility(
                    visible = selectedInfo != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.inverseSurface
                    ) {
                        Text(
                            text = selectedInfo ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            VicoWeeklyChart(
                data = data,
                onBarTapped = { info -> selectedInfo = info }
            )

            Spacer(Modifier.height(12.dp))

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                LegendItem(color = InstagramColor, label = "Instagram")
                LegendItem(color = YouTubeColor,   label = "YouTube")
                LegendItem(color = TikTokColor,    label = "TikTok")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

// ─── Vico chart (grouped columns, IG + YT per day) ───────────────────────────

@Composable
fun VicoWeeklyChart(
    data: List<UsageEntity>,
    onBarTapped: (String) -> Unit = {}
) {
    // Build the last 7 distinct dates present in data
    val allDates = data.map { it.date }.distinct().sorted().takeLast(7)

    // Per-date counts for each platform
    val igByDate = data.filter { it.platform == "Instagram" }.associateBy { it.date }
    val ytByDate = data.filter { it.platform == "YouTube"   }.associateBy { it.date }
    val ttByDate = data.filter { it.platform == "TikTok"    }.associateBy { it.date }

    val igSeries = allDates.map { (igByDate[it]?.count ?: 0).toFloat() }
    val ytSeries = allDates.map { (ytByDate[it]?.count ?: 0).toFloat() }
    val ttSeries = allDates.map { (ttByDate[it]?.count ?: 0).toFloat() }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(allDates, igSeries, ytSeries, ttSeries) {
        modelProducer.runTransaction {
            columnSeries {
                series(igSeries) // series 0 → Instagram
                series(ytSeries) // series 1 → YouTube
                series(ttSeries) // series 2 → TikTok
            }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { _, value, _ ->
        allDates.getOrNull(value.toInt())?.let { dateLabel(it) } ?: ""
    }

    // Invisible marker — we only use it to intercept taps via the listener.
    // The visual feedback comes from our own chip at the top of the card.
    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent()
    )

    val markerListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(
                marker: CartesianMarker,
                targets: List<CartesianMarker.Target>
            ) {
                val xIndex = targets.firstOrNull()?.x?.toInt() ?: return
                val date    = allDates.getOrNull(xIndex) ?: return
                val igCount = igByDate[date]?.count ?: 0
                val ytCount = ytByDate[date]?.count ?: 0
                val ttCount = ttByDate[date]?.count ?: 0
                val dayName = dateLabel(date)
                val label = buildString {
                    append(dayName); append(":  ")
                    val parts = mutableListOf<String>()
                    if (igCount > 0) parts += "IG $igCount"
                    if (ytCount > 0) parts += "YT $ytCount"
                    if (ttCount > 0) parts += "TT $ttCount"
                    append(if (parts.isEmpty()) "No data" else parts.joinToString("  ·  "))
                }
                onBarTapped(label)
            }
            override fun onHidden(marker: CartesianMarker) { /* intentionally blank */ }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    // Instagram bars
                    rememberLineComponent(
                        fill = fill(InstagramColor),
                        thickness = 10.dp,
                        shape = CorneredShape.rounded(allDp = 4f)
                    ),
                    // YouTube bars
                    rememberLineComponent(
                        fill = fill(YouTubeColor),
                        thickness = 10.dp,
                        shape = CorneredShape.rounded(allDp = 4f)
                    ),
                    // TikTok bars
                    rememberLineComponent(
                        fill = fill(TikTokColor),
                        thickness = 10.dp,
                        shape = CorneredShape.rounded(allDp = 4f)
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomAxisFormatter),
            marker = marker,
            markerVisibilityListener = markerListener
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}

// ─── Per-platform bar breakdown card ─────────────────────────────────────────

@Composable
private fun PlatformBreakdownCard(data: List<UsageEntity>) {
    val igTotal    = data.filter { it.platform == "Instagram" }.sumOf { it.count }
    val ytTotal    = data.filter { it.platform == "YouTube"   }.sumOf { it.count }
    val ttTotal    = data.filter { it.platform == "TikTok"    }.sumOf { it.count }
    val grandTotal = (igTotal + ytTotal + ttTotal).coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Platform Split",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            PlatformBar(label = "Instagram", count = igTotal, total = grandTotal, color = InstagramColor)
            PlatformBar(label = "YouTube",   count = ytTotal, total = grandTotal, color = YouTubeColor)
            PlatformBar(label = "TikTok",    count = ttTotal, total = grandTotal, color = TikTokColor)
        }
    }
}

@Composable
private fun PlatformBar(label: String, count: Int, total: Int, color: Color) {
    val fraction = count.toFloat() / total.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "$count swipes · ${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/** Converts "2025-07-08" → "Tue" — works on all API levels, no desugaring needed. */
private fun dateLabel(isoDate: String): String = try {
    val parts = isoDate.split("-")
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR,         parts[0].toInt())
        set(Calendar.MONTH,        parts[1].toInt() - 1)
        set(Calendar.DAY_OF_MONTH, parts[2].toInt())
    }
    val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    days[cal.get(Calendar.DAY_OF_WEEK) - 1]
} catch (e: Exception) {
    isoDate.takeLast(2)
}
