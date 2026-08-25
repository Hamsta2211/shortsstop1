package com.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.db.BlockedEvent
import com.example.data.preferences.BlockAction
import com.example.data.preferences.BlockerSettings
import com.example.data.repository.BlockerStats
import com.example.ui.BlockerUiState
import com.example.ui.components.RecentBlocksCard
import com.example.ui.components.ServiceStatusCard
import com.example.ui.components.SettingsCard
import com.example.ui.components.StatsOverviewCard
import com.example.ui.components.TestSimulatorCard
import com.example.ui.theme.ShortsBlockerTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun shorts_blocker_screenshot() {
    val sampleSettings = BlockerSettings(
      isProtectionActive = true,
      youtubeAction = BlockAction.REDIRECT_HOME,
      browserAction = BlockAction.PRESS_BACK,
      vibrateOnBlock = true,
      showToastOnBlock = true
    )

    val sampleStats = BlockerStats(
      todayBlocked = 8,
      totalBlocked = 24,
      youtubeBlocked = 18,
      browserBlocked = 6,
      minutesSaved = 36
    )

    val sampleEvents = listOf(
      BlockedEvent(
        id = 1,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
        appName = "YouTube App",
        packageName = "com.google.android.youtube",
        reason = "Shorts Player/Feed Element erkannt (reel_recycler)",
        actionTaken = "Zur YouTube-Startseite weitergeleitet"
      ),
      BlockedEvent(
        id = 2,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
        appName = "Google Chrome",
        packageName = "com.android.chrome",
        reason = "Shorts URL im Browser erkannt (youtube.com/shorts)",
        actionTaken = "Im Browser zurück navigiert"
      )
    )

    composeTestRule.setContent {
      ShortsBlockerTheme {
        Column(
          modifier = Modifier.fillMaxSize().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          ServiceStatusCard(
            isServiceEnabled = true,
            isProtectionActive = true,
            onToggleProtection = {},
            onOpenSettings = {},
            onShowGuide = {}
          )
          StatsOverviewCard(stats = sampleStats)
          RecentBlocksCard(events = sampleEvents, onClearHistory = {})
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
