package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.BlockAction
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
class PinProtectionTest {

    private lateinit var preferences: BlockerPreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        preferences = BlockerPreferences(context)
        preferences.removePin()
        preferences.setProtectionActive(true)
    }

    @Test
    fun `initial state has no PIN enabled`() {
        assertFalse(preferences.settings.value.isPinEnabled)
    }

    @Test
    fun `setting PIN activates PIN protection and validates correctly`() {
        preferences.setPin("1234")
        assertTrue(preferences.settings.value.isPinEnabled)

        assertTrue(preferences.validatePin("1234"))
        assertFalse(preferences.validatePin("0000"))
        assertFalse(preferences.validatePin("1235"))
    }

    @Test
    fun `removing PIN deactivates PIN protection`() {
        preferences.setPin("9876")
        assertTrue(preferences.settings.value.isPinEnabled)

        preferences.removePin()
        assertFalse(preferences.settings.value.isPinEnabled)
        assertFalse(preferences.validatePin("9876"))
    }

    @Test
    fun `changing settings updates preferences properly`() {
        preferences.setYoutubeAction(BlockAction.CLOSE_APP)
        assertEquals(BlockAction.CLOSE_APP, preferences.settings.value.youtubeAction)

        preferences.setBrowserAction(BlockAction.CLOSE_APP)
        assertEquals(BlockAction.CLOSE_APP, preferences.settings.value.browserAction)

        preferences.setVibrateOnBlock(false)
        assertFalse(preferences.settings.value.vibrateOnBlock)

        preferences.setShowToastOnBlock(false)
        assertFalse(preferences.settings.value.showToastOnBlock)
    }
}
