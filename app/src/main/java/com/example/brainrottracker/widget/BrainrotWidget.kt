package com.example.brainrottracker.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.brainrottracker.data.local.AppDatabase
import com.example.brainrottracker.data.repository.UsageRepository
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.util.NotificationHelper
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.color.ColorProvider
import androidx.compose.ui.graphics.Color
import android.content.ComponentName
import android.content.Intent
import com.example.brainrottracker.MainActivity

private val WidgetBackground = ColorProvider(
    day = Color(0xFFF3F4F6),
    night = Color(0xFF1E1E1E)
)
private val WidgetTextColor = ColorProvider(
    day = Color(0xFF111827),
    night = Color(0xFFE5E7EB)
)

class BrainrotWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Initialize repository to fetch data
        val database = AppDatabase.getDatabase(context)
        val userSettings = UserSettings(context)
        val notificationHelper = NotificationHelper(context)
        val repository = UsageRepository(database.usageDao(), userSettings, notificationHelper)
        
        provideContent {
            val count by repository.getTodayTotal().collectAsState(initial = 0)
            
            GlanceTheme {
                WidgetContent(count ?: 0)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetContent(count: Int) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetBackground)
                .clickable(actionStartActivity(Intent().setComponent(ComponentName("com.example.brainrottracker", "com.example.brainrottracker.MainActivity"))))
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Brainrot",
                style = TextStyle(
                    color = WidgetTextColor,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = count.toString(),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Swipes",
                style = TextStyle(
                    color = WidgetTextColor,
                    fontSize = 8.sp
                )
            )
        }
    }
}

class BrainrotWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BrainrotWidget()
}
