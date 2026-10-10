package com.rogue.brainrottracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY id DESC LIMIT 1")
    suspend fun getLatestSessionOnce(): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY id DESC LIMIT 1")
    fun observeLatestSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE startTime >= :dayStartMs AND startTime < :dayEndMs")
    fun getSessionsForDay(dayStartMs: Long, dayEndMs: Long): Flow<List<SessionEntity>>
}
