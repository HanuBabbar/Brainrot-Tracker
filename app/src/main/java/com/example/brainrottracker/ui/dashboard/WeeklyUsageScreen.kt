package com.example.brainrottracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brainrottracker.data.local.UsageEntity
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Last 7 Days",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (weeklyData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data available yet.")
                }
            } else {
                VicoWeeklyChart(data = weeklyData)
            }
        }
    }
}

@Composable
fun VicoWeeklyChart(data: List<UsageEntity>) {
    // Group by date and sum counts
    val dailyTotals = data.groupBy { it.date }
        .mapValues { entry -> entry.value.sumOf { it.count } }
        .toList()
        .sortedBy { it.first }
        .takeLast(7)

    // Create a Model Producer for Vico 2.0
    val modelProducer = remember { CartesianChartModelProducer() }

    // Update data whenever dailyTotals change
    LaunchedEffect(dailyTotals) {
        modelProducer.runTransaction {
            columnSeries {
                series(dailyTotals.map { it.second.toFloat() })
            }
        }
    }

    // Date formatter for the bottom axis
    val bottomAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        dailyTotals.getOrNull(value.toInt())?.first?.takeLast(2) ?: ""
    }

    val columnColor = MaterialTheme.colorScheme.primary

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = fill(columnColor),
                        thickness = 16.dp,
                        shape = CorneredShape.rounded(allDp = 4f)
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomAxisValueFormatter),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
