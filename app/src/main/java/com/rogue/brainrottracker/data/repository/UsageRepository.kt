package com.rogue.brainrottracker.data.repository

import com.rogue.brainrottracker.data.local.SessionDao
import com.rogue.brainrottracker.data.local.SessionEntity
import com.rogue.brainrottracker.data.local.UsageDao
import com.rogue.brainrottracker.data.local.UsageEntity
import com.rogue.brainrottracker.data.preferences.UserSettings
import com.rogue.brainrottracker.data.remote.NetworkClient
import com.rogue.brainrottracker.data.remote.SyncRequest
import com.rogue.brainrottracker.util.NotificationHelper
import androidx.glance.appwidget.updateAll
import com.rogue.brainrottracker.widget.BrainrotWidget
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class IncrementResult(
    val shouldBlock: Boolean,
    val totalCountToday: Int,
    val dailyLimit: Int,
)

class UsageRepository(
    private val usageDao: UsageDao,
    private val userSettings: UserSettings,
    private val notificationHelper: NotificationHelper,
    private val sessionDao: SessionDao,
) {

    @Volatile
    private var lastSyncAttemptMs: Long = 0L
    private val minSyncIntervalMs = 30_000L
    private val sessionIdleGapMs = 2 * 60 * 1000L

    private fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /** Returns the ISO date string 6 days before today (inclusive = last 7 days). */
    private fun getSevenDaysAgoCutoff(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    suspend fun incrementUsage(platform: String): IncrementResult {
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

        // Sync if logged in, throttled so a scroll burst doesn't fire a network call per swipe
        if (userSettings.userId.first() != null) {
            val now = System.currentTimeMillis()
            if (now - lastSyncAttemptMs >= minSyncIntervalMs) {
                lastSyncAttemptMs = now
                syncData()
            }
        }

        val strictModeEnabled = userSettings.strictModeEnabled.first()
        val limit = userSettings.dailyLimit.first()
        val totalCount = usageDao.getTotalCountForDateSync(date) ?: 0
        return IncrementResult(
            shouldBlock = strictModeEnabled && totalCount >= limit,
            totalCountToday = totalCount,
            dailyLimit = limit,
        )
    }

    /**
     * Extends the most recent session if it's the same platform and within the idle gap,
     * otherwise closes it out (implicitly, by starting a fresh row) and opens a new one.
     * Returns true if a brand-new session was started (i.e. the prior one just ended).
     */
    suspend fun trackSwipeForSession(platform: String): Boolean {
        val now = System.currentTimeMillis()
        val latest = sessionDao.getLatestSessionOnce()
        val lastActivity = latest?.endTime ?: latest?.startTime

        return if (latest != null && latest.platform == platform && lastActivity != null && now - lastActivity <= sessionIdleGapMs) {
            sessionDao.updateSession(latest.copy(endTime = now, swipeCount = latest.swipeCount + 1))
            false
        } else {
            sessionDao.insertSession(SessionEntity(platform = platform, startTime = now, endTime = now, swipeCount = 1))
            latest != null
        }
    }

    /** Active session age in minutes if the latest session is still "warm" (within the idle gap), else null. */
    fun observeActiveSessionMinutes(): Flow<SessionEntity?> = sessionDao.observeLatestSession()
        .map { session ->
            val lastActivity = session?.endTime ?: session?.startTime
            if (session != null && lastActivity != null && System.currentTimeMillis() - lastActivity <= sessionIdleGapMs) session else null
        }

    fun getSessionsForDate(dateMillisStart: Long, dateMillisEnd: Long): Flow<List<SessionEntity>> =
        sessionDao.getSessionsForDay(dateMillisStart, dateMillisEnd)

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
        // Only sync when logged in
        if (userSettings.userId.first() == null) return
        val stats = usageDao.getWeeklyUsage(getSevenDaysAgoCutoff()).first()

        try {
            // Use the BASE_URL from NetworkClient
            val url = "${NetworkClient.BASE_URL}sync"
            val response = NetworkClient.client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(SyncRequest(stats))
            }
            android.util.Log.d("UsageRepository", "Sync successful: ${response.status}")
        } catch (e: Exception) {
            android.util.Log.e("UsageRepository", "Sync failed", e)
        }
    }

    suspend fun pullData() {
        // Only pull when logged in
        if (userSettings.userId.first() == null) return

        try {
            // No userId query param — the server reads it from the JWT Bearer token
            val url = "${NetworkClient.BASE_URL}sync/pull"
            val response = NetworkClient.client.get(url)
            val pullResponse = response.body<com.rogue.brainrottracker.data.remote.PullResponse>()

            // Merge: for each cloud entry, only overwrite local if cloud count is higher
            for (stat in pullResponse.stats) {
                val local = usageDao.getUsageByDate(stat.date, stat.platform)
                if (local == null || stat.count > local.count) {
                    usageDao.upsertUsage(
                        UsageEntity(
                            id = local?.id ?: 0,
                            date = stat.date,
                            platform = stat.platform,
                            count = stat.count
                        )
                    )
                }
            }
            updateWidgets()
        } catch (e: Exception) {
            android.util.Log.e("UsageRepository", "Pull failed", e)
        }
    }

    suspend fun updateWidgets() {
        notificationHelper.getContext().let { context ->
            BrainrotWidget().updateAll(context)
            com.rogue.brainrottracker.widget.BrainrotMeterWidget().updateAll(context)
        }
    }
}
