package com.example.brainrottracker.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    @Upsert
    suspend fun upsertUsage(usage: UsageEntity)

    @Query("SELECT * FROM usage_stats WHERE date = :date AND platform = :platform LIMIT 1")
    suspend fun getUsageByDate(date: String, platform: String): UsageEntity?

    @Query("SELECT * FROM usage_stats ORDER BY date DESC LIMIT 7")
    fun getWeeklyUsage(): Flow<List<UsageEntity>>

    @Query("SELECT SUM(count) FROM usage_stats WHERE date = :date")
    fun getTOtalCountForDate(date: String): Flow<Int?>



}

