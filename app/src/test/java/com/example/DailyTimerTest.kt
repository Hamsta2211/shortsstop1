package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.BlockerPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DailyTimerTest {

    private lateinit var preferences: BlockerPreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        preferences = BlockerPreferences(context)
        preferences.setDailyTimerEnabled(false)
        preferences.resetDailyUsedSeconds()
    }

    @Test
    fun `default daily timer is disabled and customizable`() {
        assertFalse(preferences.settings.value.isDailyTimerEnabled)
        preferences.setDailyTimerEnabled(true)
        assertTrue(preferences.settings.value.isDailyTimerEnabled)

        preferences.setDailyLimitMinutes(20)
        assertEquals(20, preferences.settings.value.dailyLimitMinutes)
        assertEquals(1200L, preferences.settings.value.totalLimitSeconds)
    }

    @Test
    fun `tracking seconds increments used quota and detects limit reached`() {
        preferences.setDailyTimerEnabled(true)
        preferences.setDailyLimitMinutes(2) // 120 seconds

        assertFalse(preferences.settings.value.isLimitReached)
        assertEquals(120L, preferences.settings.value.remainingSecondsToday)

        preferences.addUsedShortsSeconds(60)
        assertEquals(60L, preferences.settings.value.usedSecondsToday)
        assertEquals(60L, preferences.settings.value.remainingSecondsToday)
        assertEquals(0.5f, preferences.settings.value.usageProgress, 0.01f)
        assertFalse(preferences.settings.value.isLimitReached)

        // Exceed limit
        preferences.addUsedShortsSeconds(60)
        assertEquals(120L, preferences.settings.value.usedSecondsToday)
        assertEquals(0L, preferences.settings.value.remainingSecondsToday)
        assertTrue(preferences.settings.value.isLimitReached)

        // Reset
        preferences.resetDailyUsedSeconds()
        assertEquals(0L, preferences.settings.value.usedSecondsToday)
        assertEquals(120L, preferences.settings.value.remainingSecondsToday)
        assertFalse(preferences.settings.value.isLimitReached)
    }
}
