package com.example.data.repository

import com.example.data.db.BlockedEvent
import com.example.data.db.BlockedEventDao
import com.example.data.preferences.BlockAction
import com.example.data.preferences.BlockerPreferences
import com.example.data.preferences.BlockerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

data class BlockerStats(
    val todayBlocked: Int = 0,
    val totalBlocked: Int = 0,
    val youtubeBlocked: Int = 0,
    val browserBlocked: Int = 0,
    val minutesSaved: Int = 0
)

class BlockerRepository(
    private val dao: BlockedEventDao,
    private val preferences: BlockerPreferences
) {
    val settings: StateFlow<BlockerSettings> = preferences.settings

    val recentEvents: Flow<List<BlockedEvent>> = dao.getRecentEvents(30)
    val totalEventsCount: Flow<Int> = dao.getTotalCount()

    fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getTodayEventsCount(): Flow<Int> {
        return dao.getTodayCount(getTodayStartTimestamp())
    }

    fun getYoutubeCount(): Flow<Int> = dao.getYoutubeCount()
    fun getBrowserCount(): Flow<Int> = dao.getBrowserCount()

    suspend fun recordBlockedEvent(
        appName: String,
        packageName: String,
        reason: String,
        actionTaken: String
    ) {
        val event = BlockedEvent(
            appName = appName,
            packageName = packageName,
            reason = reason,
            actionTaken = actionTaken
        )
        dao.insertEvent(event)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }

    fun setProtectionActive(active: Boolean) = preferences.setProtectionActive(active)
    fun setYoutubeAction(action: BlockAction) = preferences.setYoutubeAction(action)
    fun setBrowserAction(action: BlockAction) = preferences.setBrowserAction(action)
    fun setVibrateOnBlock(enabled: Boolean) = preferences.setVibrateOnBlock(enabled)
    fun setShowToastOnBlock(enabled: Boolean) = preferences.setShowToastOnBlock(enabled)
    fun setBlockShortsShelves(enabled: Boolean) = preferences.setBlockShortsShelves(enabled)
    fun setDailyTimerEnabled(enabled: Boolean) = preferences.setDailyTimerEnabled(enabled)
    fun setDailyLimitMinutes(minutes: Int) = preferences.setDailyLimitMinutes(minutes)
    fun addUsedShortsSeconds(seconds: Long) = preferences.addUsedShortsSeconds(seconds)
    fun resetDailyUsedSeconds() = preferences.resetDailyUsedSeconds()
    fun setPin(pin: String) = preferences.setPin(pin)
    fun removePin() = preferences.removePin()
    fun validatePin(pin: String): Boolean = preferences.validatePin(pin)
}
