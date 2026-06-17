package com.example.brainrottracker.data.repository



import com.example.brainrottracker.data.local.UsageDao
import com.example.brainrottracker.data.local.UsageEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UsageRepository(private val usageDao: UsageDao) {

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun incrementUsage(platform: String){
        val date = getTodayDate()

        val existingUsage = usageDao.getUsageByDate(date, platform)

        if (existingUsage != null){
            val updateUsage = existingUsage.copy(count = existingUsage.count +1)
            usageDao.upsertUsage(updateUsage)
        }else{
            val newUsage = UsageEntity(
                date = date,
                platform = platform,
                count = 1
            )
            usageDao.upsertUsage(newUsage)
        }
    }

    fun getWeekly():  Flow<List<UsageEntity>> = usageDao.getWeeklyUsage()

    fun getTodayTotal(): Flow<Int?> = usageDao.getTOtalCountForDate(getTodayDate())

}