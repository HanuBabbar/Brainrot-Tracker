package com.rogue.brainrottracker.data.repository

import com.rogue.brainrottracker.data.local.UsageDao
import com.rogue.brainrottracker.data.local.UsageEntity
import com.rogue.brainrottracker.data.preferences.UserSettings
import com.rogue.brainrottracker.util.NotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageRepositoryTest {

    private lateinit var usageDao: UsageDao
    private lateinit var userSettings: UserSettings
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var repository: UsageRepository

    @Before
    fun setup() {
        usageDao = mockk(relaxed = true)
        userSettings = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)
        
        // Mock default flows
        every { userSettings.dailyLimit } returns flowOf(100)
        every { userSettings.lastNotifiedDate } returns flowOf("2000-01-01")
        every { userSettings.vibrationEnabled } returns flowOf(false)
        every { userSettings.userId } returns flowOf(null) // Not logged in
        
        repository = spyk(UsageRepository(usageDao, userSettings, notificationHelper))
        
        // Mock updateWidgets to do nothing in tests to avoid Glance UI context crashes
        coEvery { repository.updateWidgets() } returns Unit
    }

    private fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    @Test
    fun `incrementUsage creates new entry if none exists`() = runTest {
        val platform = "Instagram"
        val date = getTodayDate()
        
        coEvery { usageDao.getUsageByDate(date, platform) } returns null
        coEvery { usageDao.getTotalCountForDateSync(date) } returns 0
        
        repository.incrementUsage(platform)
        
        coVerify { 
            usageDao.upsertUsage(match { 
                it.platform == platform && it.count == 1 && it.date == date
            })
        }
    }

    @Test
    fun `incrementUsage updates existing entry`() = runTest {
        val platform = "Instagram"
        val date = getTodayDate()
        val existingEntry = UsageEntity(id = 1, date = date, platform = platform, count = 5)
        
        coEvery { usageDao.getUsageByDate(date, platform) } returns existingEntry
        coEvery { usageDao.getTotalCountForDateSync(date) } returns 5
        
        repository.incrementUsage(platform)
        
        coVerify { 
            usageDao.upsertUsage(match { 
                it.platform == platform && it.count == 6 && it.date == date
            })
        }
    }

    @Test
    fun `checkLimitAndNotify triggers notification when limit reached`() = runTest {
        val platform = "Instagram"
        val date = getTodayDate()
        
        every { userSettings.dailyLimit } returns flowOf(10)
        coEvery { usageDao.getUsageByDate(date, platform) } returns null
        coEvery { usageDao.getTotalCountForDateSync(date) } returns 10 // Mock total count as limit reached
        
        repository.incrementUsage(platform)
        
        coVerify { 
            notificationHelper.sendLimitReachedNotification(10, false)
        }
    }
}
