package com.example.brainrottracker.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.action.ActionParameters
import kotlinx.coroutines.flow.first
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.LinearProgressIndicator
import com.example.brainrottracker.data.local.AppDatabase
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.repository.UsageRepository
import com.example.brainrottracker.util.NotificationHelper

private val WidgetBackground = ColorProvider(
    day = Color(0xFFFBFDF9),
    night = Color(0xFF191C1A)
)
private val WidgetTextColor = ColorProvider(
    day = Color(0xFF191C1A),
    night = Color(0xFFE1E3DF)
)
private val WidgetPrimaryColor = ColorProvider(
    day = Color(0xFF006C4C),
    night = Color(0xFF6CDBAC)
)

class BrainrotMeterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val userSettings = UserSettings(context)
        val notificationHelper = NotificationHelper(context)
        val repository = UsageRepository(database.usageDao(), userSettings, notificationHelper)
        
        provideContent {
            val count by repository.getTodayTotal().collectAsState(initial = 0)
            val limit by userSettings.dailyLimit.collectAsState(initial = 100)
            val strictMode by userSettings.strictModeEnabled.collectAsState(initial = false)
            
            GlanceTheme {
                WidgetContent(count ?: 0, limit, strictMode)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetContent(count: Int, limit: Int, strictMode: Boolean) {
        val progress = if (limit > 0) (count.toFloat() / limit).coerceIn(0f, 1f) else 0f
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetBackground)
                .clickable(actionStartActivity(Intent().setComponent(ComponentName("com.example.brainrottracker", "com.example.brainrottracker.MainActivity"))))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Limit",
                    style = TextStyle(color = WidgetTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "$count / $limit",
                    style = TextStyle(color = WidgetTextColor, fontSize = 12.sp)
                )
            }
            Spacer(modifier = GlanceModifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = GlanceModifier.fillMaxWidth(),
                color = WidgetPrimaryColor,
                backgroundColor = ColorProvider(
                    day = Color(0xFFDBE5DD),
                    night = Color(0xFF404943)
                )
            )
            Spacer(modifier = GlanceModifier.height(12.dp))
            Button(
                text = if (strictMode) "Strict Mode: ON" else "Strict Mode: OFF",
                onClick = actionRunCallback<ToggleStrictModeAction>(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (strictMode) WidgetPrimaryColor else ColorProvider(day = Color(0xFFDBE5DD), night = Color(0xFF404943)),
                    contentColor = if (strictMode) ColorProvider(Color.White, Color.Black) else WidgetTextColor
                ),
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

class ToggleStrictModeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val userSettings = UserSettings(context)
        val currentState = userSettings.strictModeEnabled.first()
        userSettings.setStrictModeEnabled(!currentState)
        BrainrotMeterWidget().update(context, glanceId)
    }
}

class BrainrotMeterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BrainrotMeterWidget()
}
