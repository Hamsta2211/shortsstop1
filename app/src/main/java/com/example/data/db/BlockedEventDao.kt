package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedEventDao {
    @Query("SELECT * FROM blocked_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<BlockedEvent>>

    @Query("SELECT * FROM blocked_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<BlockedEvent>>

    @Query("SELECT COUNT(*) FROM blocked_events")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_events WHERE timestamp >= :startOfDayTimestamp")
    fun getTodayCount(startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_events WHERE packageName = 'com.google.android.youtube'")
    fun getYoutubeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_events WHERE packageName != 'com.google.android.youtube'")
    fun getBrowserCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: BlockedEvent): Long

    @Query("DELETE FROM blocked_events")
    suspend fun clearAll()
}
