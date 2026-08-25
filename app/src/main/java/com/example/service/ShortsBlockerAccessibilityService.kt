package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.example.data.db.AppDatabase
import com.example.data.preferences.BlockAction
import com.example.data.preferences.BlockerPreferences
import com.example.data.repository.BlockerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ShortsBlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var preferences: BlockerPreferences
    private lateinit var repository: BlockerRepository

    private var lastBlockTime = 0L
    private val blockCooldownMs = 1600L

    private var shortsTrackingJob: Job? = null
    @Volatile
    private var isCurrentlyInShorts = false
    @Volatile
    private var currentPackageName: String? = null

    override fun onCreate() {
        super.onCreate()
        preferences = BlockerPreferences.getInstance(this)
        val database = AppDatabase.getInstance(this)
        repository = BlockerRepository(database.blockedEventDao(), preferences)
        NotificationHelper.createNotificationChannels(this)
        _isServiceActive.value = true
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        NotificationHelper.createNotificationChannels(this)
        _isServiceActive.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val settings = preferences.settings.value
        if (!settings.isProtectionActive) {
            stopShortsTracking()
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (!ShortsDetector.isSupportedApp(packageName)) {
            stopShortsTracking()
            return
        }

        val rootNode = try {
            rootInActiveWindow
        } catch (e: Exception) {
            null
        } ?: return

        try {
            val detection = ShortsDetector.detectShorts(rootNode, packageName)
            if (detection.isShorts) {
                currentPackageName = packageName

                // Check if daily timer is enabled
                if (settings.isDailyTimerEnabled) {
                    if (settings.isLimitReached) {
                        stopShortsTracking()
                        val now = System.currentTimeMillis()
                        if (now - lastBlockTime >= blockCooldownMs) {
                            lastBlockTime = now
                            executeBlockAction(
                                packageName,
                                "Tägliches Limit (${settings.dailyLimitMinutes} Min.) abgelaufen",
                                rootNode
                            )
                        }
                    } else {
                        // User still has daily quota remaining -> allow and track time
                        startShortsTracking(packageName)
                    }
                } else {
                    // Standard immediate block
                    stopShortsTracking()
                    val now = System.currentTimeMillis()
                    if (now - lastBlockTime >= blockCooldownMs) {
                        lastBlockTime = now
                        executeBlockAction(packageName, detection.reason, rootNode)
                    }
                }
            } else {
                // Not in Shorts screen
                stopShortsTracking()
            }
        } catch (e: Exception) {
            // Gracefully handle node inspection exceptions
        }
    }

    private fun startShortsTracking(packageName: String) {
        isCurrentlyInShorts = true
        if (shortsTrackingJob?.isActive == true) return

        shortsTrackingJob = serviceScope.launch {
            while (isActive && isCurrentlyInShorts) {
                delay(1000L)
                if (!isCurrentlyInShorts) break

                val currentSettings = preferences.settings.value
                if (!currentSettings.isDailyTimerEnabled || !currentSettings.isProtectionActive) {
                    break
                }

                val usedSeconds = preferences.addUsedShortsSeconds(1L)
                if (usedSeconds >= currentSettings.totalLimitSeconds) {
                    // Limit reached right while watching!
                    isCurrentlyInShorts = false

                    mainHandler.post {
                        // 1. Trigger Notification
                        NotificationHelper.showTimeExpiredNotification(
                            applicationContext,
                            currentSettings.dailyLimitMinutes
                        )

                        // 2. Execute block action
                        val root = try { rootInActiveWindow } catch (e: Exception) { null }
                        if (root != null) {
                            executeBlockAction(
                                packageName,
                                "Tägliches Limit (${currentSettings.dailyLimitMinutes} Min.) erreicht",
                                root
                            )
                        } else {
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        }
                    }
                    break
                }
            }
        }
    }

    private fun stopShortsTracking() {
        isCurrentlyInShorts = false
        shortsTrackingJob?.cancel()
        shortsTrackingJob = null
    }

    private fun executeBlockAction(packageName: String, reason: String, rootNode: AccessibilityNodeInfo) {
        val settings = preferences.settings.value
        val appName = ShortsDetector.getAppDisplayName(packageName)
        val isYouTube = packageName == ShortsDetector.PACKAGE_YOUTUBE

        val targetAction = if (isYouTube) settings.youtubeAction else settings.browserAction
        var executedActionDescription = ""

        when (targetAction) {
            BlockAction.REDIRECT_HOME -> {
                if (isYouTube) {
                    val homeTab = ShortsDetector.findYouTubeHomeTab(rootNode)
                    if (homeTab != null) {
                        homeTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        executedActionDescription = "Zur YouTube-Startseite weitergeleitet"
                    } else {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        executedActionDescription = "Zurück navigiert (Home-Tab nicht erreichbar)"
                    }
                } else {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    executedActionDescription = "Im Browser zurück navigiert"
                }
            }
            BlockAction.PRESS_BACK -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
                executedActionDescription = "Zurück-Taste ausgeführt"
            }
            BlockAction.CLOSE_APP -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
                executedActionDescription = "App geschlossen (Homescreen)"
            }
        }

        // Haptic Feedback
        if (settings.vibrateOnBlock) {
            triggerHapticFeedback()
        }

        // Toast Feedback
        if (settings.showToastOnBlock) {
            mainHandler.post {
                Toast.makeText(
                    applicationContext,
                    "🚫 Shorts blockiert: $executedActionDescription",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Record in database
        serviceScope.launch {
            repository.recordBlockedEvent(
                appName = appName,
                packageName = packageName,
                reason = reason,
                actionTaken = executedActionDescription
            )
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(70)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    override fun onInterrupt() {
        _isServiceActive.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceActive.value = false
        serviceScope.cancel()
    }

    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()

        fun markServiceActive(active: Boolean) {
            _isServiceActive.value = active
        }
    }
}
