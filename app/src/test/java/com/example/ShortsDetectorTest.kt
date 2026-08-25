package com.example

import com.example.service.ShortsDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortsDetectorTest {

    @Test
    fun isSupportedApp_youtube_returnsTrue() {
        assertTrue(ShortsDetector.isSupportedApp("com.google.android.youtube"))
    }

    @Test
    fun isSupportedApp_chrome_returnsTrue() {
        assertTrue(ShortsDetector.isSupportedApp("com.android.chrome"))
    }

    @Test
    fun isSupportedApp_firefox_returnsTrue() {
        assertTrue(ShortsDetector.isSupportedApp("org.mozilla.firefox"))
    }

    @Test
    fun isSupportedApp_otherApp_returnsFalse() {
        assertFalse(ShortsDetector.isSupportedApp("com.example.calculator"))
    }

    @Test
    fun getAppDisplayName_youtube_returnsFormattedName() {
        assertEquals("YouTube App", ShortsDetector.getAppDisplayName("com.google.android.youtube"))
        assertEquals("Google Chrome", ShortsDetector.getAppDisplayName("com.android.chrome"))
        assertEquals("Mozilla Firefox", ShortsDetector.getAppDisplayName("org.mozilla.firefox"))
    }
}
