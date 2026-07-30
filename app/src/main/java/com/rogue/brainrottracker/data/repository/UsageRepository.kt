package com.rogue.brainrottracker.data.repository

import com.rogue.brainrottracker.data.local.UsageDao
import com.rogue.brainrottracker.data.local.UsageEntity
import com.rogue.brainrottracker.data.preferences.UserSettings
import com.rogue.brainrottracker.data.remote.NetworkClient
import com.rogue.brainrottracker.data.remote.SyncRequest
import com.rogue.brainrottracker.util.NotificationHelper
import androidx.glance.appwidget.updateAll
import com.rogue.brainrottracker.widget.BrainrotWidget
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UsageRepository(
    private val usageDao: UsageDao,
    private val userSettings: UserSettings,
    private val notificationHelper: NotificationHelper
) {

    private fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /** Returns the ISO date string 6 days before today (inclusive = last 7 days). */
    private fun getSevenDaysAgoCutoff(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    suspend fun incrementUsage(platform: String): Boolean {
        val date = getTodayDate()
        val existingUsage = usageDao.getUsageByDate(date, platform)

        if (existingUsage != null) {
            val updatedUsage = existingUsage.copy(count = existingUsage.count + 1)
            usageDao.upsertUsage(updatedUsage)
        } else {
            val newUsage = UsageEntity(
                date = date,
                platform = platform,
                count = 1
            )
            usageDao.upsertUsage(newUsage)
        }
        
        // Update widgets whenever data changes
        updateWidgets()

        checkLimitAndNotify()

        // Sync if logged in
        if (userSettings.userId.first() != null) {
            syncData()
        }

        val strictModeEnabled = userSettings.strictModeEnabled.first()
        val limit = userSettings.dailyLimit.first()
        val totalCount = usageDao.getTotalCountForDateSync(date) ?: 0
        return strictModeEnabled && totalCount >= limit
    }

    private suspend fun checkLimitAndNotify() {
        val date = getTodayDate()
        val totalCount = usageDao.getTotalCountForDateSync(date) ?: 0
        val limit = userSettings.dailyLimit.first()
        val lastNotifiedDate = userSettings.lastNotifiedDate.first()
        val vibrate = userSettings.vibrationEnabled.first()
        
        android.util.Log.d("BrainrotTracker", "Limit Check: Total=$totalCount, Limit=$limit, LastNotified=$lastNotifiedDate, Date=$date")

        if (totalCount >= limit && lastNotifiedDate != date) {
            android.util.Log.d("BrainrotTracker", "CONDITION MET: Triggering notification!")
            notificationHelper.sendLimitReachedNotification(totalCount, vibrate)
            userSettings.setLastNotifiedDate(date)
        } else {
            android.util.Log.d("BrainrotTracker", "CONDITION NOT MET: totalCount < limit OR already notified today")
        }
    }

    fun getWeekly(): Flow<List<UsageEntity>> = usageDao.getWeeklyUsage(getSevenDaysAgoCutoff())

    fun getTodayTotal(): Flow<Int?> = usageDao.getTotalCountForDate(getTodayDate())

    suspend fun syncData() {
        val userId = userSettings.userId.first() ?: return
        val stats = usageDao.getWeeklyUsage(getSevenDaysAgoCutoff()).first()

        try {
            // Use the BASE_URL from NetworkClient
            val url = "${NetworkClient.BASE_URL}sync"
            val response = NetworkClient.client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(SyncRequest(userId, stats))
            }
            android.util.Log.d("UsageRepository", "Sync successful: ${response.status}")
        } catch (e: Exception) {
            android.util.Log.e("UsageRepository", "Sync failed", e)
        }
    }

    suspend fun updateWidgets() {
        notificationHelper.getContext().let { context ->
            BrainrotWidget().updateAll(context)
            com.rogue.brainrottracker.widget.BrainrotMeterWidget().updateAll(context)
        }
    }
}
