package com.example.brainrottracker.data.repository

import com.example.brainrottracker.data.local.UsageDao
import com.example.brainrottracker.data.local.UsageEntity
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.remote.NetworkClient
import com.example.brainrottracker.data.remote.SyncRequest
import com.example.brainrottracker.util.NotificationHelper
import androidx.glance.appwidget.updateAll
import com.example.brainrottracker.widget.BrainrotWidget
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageRepository(
    private val usageDao: UsageDao,
    private val userSettings: UserSettings,
    private val notificationHelper: NotificationHelper
) {

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun incrementUsage(platform: String) {
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
        notificationHelper.getContext().let { context ->
            BrainrotWidget().updateAll(context)
        }

        checkLimitAndNotify()

        // Sync if logged in
        if (userSettings.userId.first() != null) {
            syncData()
        }
    }

    private suspend fun checkLimitAndNotify() {
        val date = getTodayDate()
        val totalCount = usageDao.getTotalCountForDateSync(date) ?: 0
        val limit = userSettings.dailyLimit.first()
        val lastNotifiedDate = userSettings.lastNotifiedDate.first()
        
        android.util.Log.d("BrainrotTracker", "Limit Check: Total=$totalCount, Limit=$limit, LastNotified=$lastNotifiedDate, Date=$date")

        if (totalCount >= limit && lastNotifiedDate != date) {
            android.util.Log.d("BrainrotTracker", "CONDITION MET: Triggering notification!")
            notificationHelper.sendLimitReachedNotification(totalCount)
            userSettings.setLastNotifiedDate(date)
        } else {
            android.util.Log.d("BrainrotTracker", "CONDITION NOT MET: totalCount < limit OR already notified today")
        }
    }

    fun getWeekly(): Flow<List<UsageEntity>> = usageDao.getWeeklyUsage()

    fun getTodayTotal(): Flow<Int?> = usageDao.getTotalCountForDate(getTodayDate())

    suspend fun syncData() {
        val userId = userSettings.userId.first() ?: return
        val stats = usageDao.getWeeklyUsage().first()

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
}
