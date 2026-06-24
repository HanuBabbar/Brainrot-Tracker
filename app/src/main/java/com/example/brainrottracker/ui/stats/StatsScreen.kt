package com.example.brainrottracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.data.local.UsageEntity
import com.example.brainrottracker.ui.AppViewModel
import com.example.brainrottracker.ui.components.BrainRotCard
import com.example.brainrottracker.ui.components.PageHeader
import com.example.brainrottracker.ui.components.PeriodToggle
import com.example.brainrottracker.ui.dashboard.WeeklyUsageViewModel
import com.example.brainrottracker.ui.theme.*
import com.example.brainrottracker.util.formatDurationMs
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun StatsScreen(viewModel: WeeklyUsageViewModel, appViewModel: AppViewModel) {
    val weeklyData by viewModel.weeklyUsage.collectAsState()
    val longestSessionMs by appViewModel.longestSessionMs.collectAsState()
    var showWeek by remember { mutableStateOf(true) }

    val dailyTotals    = remember(weeklyData) { aggregateDailyTotals(weeklyData) }
    val weekChartData  = remember(dailyTotals) { buildWeekChartData(dailyTotals) }
    val monthChartData = remember(dailyTotals) { buildMonthChartData(dailyTotals) }
    val chartData      = if (showWeek) weekChartData else monthChartData
    val total          = chartData.sumOf { it.second }

    val bestDay   = weekChartData.maxByOrNull { it.second }
    val avgPerDay = if (weekChartData.isNotEmpty())
        weekChartData.map { it.second }.average().toInt() else 0

    // label colours
    val labelColor    = MutedForeground
    val barColor      = PrimaryPurple
    val chartBg       = CardDark
    val fgColor       = ForegroundLight
    val lightPurpleClr = LightPurple

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        PageHeader(overline = "Overview", title = "Your Stats")

        // ── Total bubble ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0x996D28D9), Color(0x4D312E81)))
                )
                .border(1.dp, Color(0x4D7C3AED), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (showWeek) "THIS WEEK" else "THIS MONTH",
                fontSize = 10.sp,
                color = lightPurpleClr.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
            Text(
                text = total.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = fgColor
            )
            Text("reels scrolled", fontSize = 11.sp, color = lightPurpleClr.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Period toggle ─────────────────────────────────────────────────────
        PeriodToggle(
            selectedWeek    = showWeek,
            onWeekSelected  = { showWeek = true },
            onMonthSelected = { showWeek = false }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Chart card ────────────────────────────────────────────────────────
        BrainRotCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text = "REELS PER DAY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = fgColor.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (showWeek) "7 days" else "21 days",
                    fontSize = 10.sp,
                    color = labelColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chartData.isEmpty() || chartData.all { it.second == 0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No data available yet.",
                        color = labelColor,
                        fontSize = 13.sp
                    )
                }
            } else {
                StatsBarChart(
                    data     = chartData,
                    barWidth = if (showWeek) 28.dp else 8.dp,
                    labelColor = labelColor,
                    barColor   = barColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Summary cards ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatSummaryCard(
                value    = bestDay?.second?.toString() ?: "—",
                label    = "Best Day",
                sub      = bestDay?.first ?: "—",
                modifier = Modifier.weight(1f)
            )
            StatSummaryCard(
                value    = avgPerDay.toString(),
                label    = "Avg / Day",
                sub      = if (showWeek) "This week" else "This month",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatSummaryCard(
                value    = "11 PM",
                label    = "Peak Hour",
                sub      = "Most active",
                modifier = Modifier.weight(1f)
            )
            StatSummaryCard(
                value    = formatDurationMs(longestSessionMs),
                label    = "Longest Session",
                sub      = "Recent",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ── StatSummaryCard ───────────────────────────────────────────────────────────
@Composable
private fun StatSummaryCard(
    value: String,
    label: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    val fg     = ForegroundLight
    val lp     = LightPurple
    val muted  = MutedForeground
    val card   = CardDark
    val border = BorderPurple

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(card)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = fg)
        Text(label, fontSize = 10.sp, color = lp, fontWeight = FontWeight.SemiBold)
        Text(sub,   fontSize = 9.sp,  color = muted)
    }
}

// ── Bar chart ─────────────────────────────────────────────────────────────────
@Composable
private fun StatsBarChart(
    data: List<Pair<String, Int>>,
    barWidth: androidx.compose.ui.unit.Dp,
    labelColor: Color,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(data.map { it.second.toFloat() })
            }
        }
    }

    val labelTextComponent = rememberTextComponent(
        color = labelColor,
        textSize = 10.sp
    )

    androidx.compose.runtime.key(data, barWidth) {
        val bottomAxisFormatter = CartesianValueFormatter { _, value, _ ->
            data.getOrNull(value.toInt())?.first ?: ""
        }

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill      = fill(barColor),
                            thickness = barWidth,
                            shape     = CorneredShape.rounded(allDp = 6f)
                        )
                    )
                ),
                // No startAxis → removes Y-axis line and horizontal gridlines
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = bottomAxisFormatter,
                    label          = labelTextComponent,
                    guideline      = null,  // removes vertical tick lines
                    line           = null,  // removes the bottom axis line itself
                    tick           = null   // removes bottom tick marks
                )
            ),
            modelProducer = modelProducer,
            modifier      = modifier
        )
    }
}

// ── Data helpers ──────────────────────────────────────────────────────────────
private fun aggregateDailyTotals(data: List<UsageEntity>): Map<String, Int> =
    data.groupBy { it.date }
        .mapValues { (_, entries) -> entries.sumOf { it.count } }

private fun buildWeekChartData(dailyTotals: Map<String, Int>): List<Pair<String, Int>> {
    val calendar   = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val result     = mutableListOf<Pair<String, Int>>()
    for (i in 6 downTo 0) {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        val dateKey  = dateFormat.format(calendar.time)
        val dayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        result.add(dayLabels[dayIndex] to (dailyTotals[dateKey] ?: 0))
    }
    return result
}

private fun buildMonthChartData(dailyTotals: Map<String, Int>): List<Pair<String, Int>> {
    val calendar   = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val result     = mutableListOf<Pair<String, Int>>()
    for (i in 20 downTo 0) {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        val dateKey = dateFormat.format(calendar.time)
        val dayNum  = calendar.get(Calendar.DAY_OF_MONTH).toString()
        result.add(dayNum to (dailyTotals[dateKey] ?: 0))
    }
    return result
}