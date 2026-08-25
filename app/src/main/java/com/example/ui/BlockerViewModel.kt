package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.BlockedEvent
import com.example.data.preferences.BlockAction
import com.example.data.preferences.BlockerPreferences
import com.example.data.preferences.BlockerSettings
import com.example.data.repository.BlockerRepository
import com.example.data.repository.BlockerStats
import com.example.service.AccessibilityUtils
import com.example.service.ShortsBlockerAccessibilityService
import com.example.ui.components.PinDialogAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BlockerUiState(
    val isAccessibilityServiceEnabled: Boolean = false,
    val settings: BlockerSettings = BlockerSettings(),
    val stats: BlockerStats = BlockerStats(),
    val recentEvents: List<BlockedEvent> = emptyList(),
    val showSetupDialog: Boolean = false,
    val pinDialogAction: PinDialogAction? = null
)

class BlockerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val preferences = BlockerPreferences.getInstance(application)
    private val repository = BlockerRepository(database.blockedEventDao(), preferences)

    private val _isAccessibilityEnabled = MutableStateFlow(
        AccessibilityUtils.isAccessibilityServiceEnabled(application)
    )
    private val _showSetupDialog = MutableStateFlow(false)
    private val _pinDialogAction = MutableStateFlow<PinDialogAction?>(null)

    val uiState: StateFlow<BlockerUiState> = combine(
        _isAccessibilityEnabled,
        preferences.settings,
        repository.getTodayEventsCount(),
        repository.totalEventsCount,
        repository.getYoutubeCount(),
        repository.getBrowserCount(),
        repository.recentEvents,
        _showSetupDialog,
        _pinDialogAction
    ) { params: Array<Any?> ->
        val isServiceEnabled = params[0] as Boolean
        val settings = params[1] as BlockerSettings
        val todayCount = params[2] as Int
        val totalCount = params[3] as Int
        val ytCount = params[4] as Int
        val brCount = params[5] as Int
        @Suppress("UNCHECKED_CAST")
        val events = params[6] as List<BlockedEvent>
        val showDialog = params[7] as Boolean
        val pinAction = params[8] as? PinDialogAction

        val stats = BlockerStats(
            todayBlocked = todayCount,
            totalBlocked = totalCount,
            youtubeBlocked = ytCount,
            browserBlocked = brCount,
            minutesSaved = (totalCount * 1.5).toInt()
        )

        BlockerUiState(
            isAccessibilityServiceEnabled = isServiceEnabled,
            settings = settings,
            stats = stats,
            recentEvents = events,
            showSetupDialog = showDialog,
            pinDialogAction = pinAction
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BlockerUiState(
            isAccessibilityServiceEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(application)
        )
    )

    fun refreshAccessibilityStatus(context: Context) {
        val enabled = AccessibilityUtils.isAccessibilityServiceEnabled(context)
        _isAccessibilityEnabled.value = enabled
        ShortsBlockerAccessibilityService.markServiceActive(enabled)
    }

    /**
     * Toggles protection. If attempting to turn OFF protection while PIN is enabled,
     * requires authentication first.
     */
    fun toggleProtection(active: Boolean) {
        if (!active && preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToDisableProtection(
                onVerified = {
                    repository.setProtectionActive(false)
                }
            )
        } else {
            repository.setProtectionActive(active)
        }
    }

    fun setYoutubeAction(action: BlockAction) {
        if (preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToChangeSettings(
                description = "PIN erforderlich, um die YouTube-Aktion zu ändern."
            ) {
                repository.setYoutubeAction(action)
            }
        } else {
            repository.setYoutubeAction(action)
        }
    }

    fun setBrowserAction(action: BlockAction) {
        if (preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToChangeSettings(
                description = "PIN erforderlich, um die Browser-Aktion zu ändern."
            ) {
                repository.setBrowserAction(action)
            }
        } else {
            repository.setBrowserAction(action)
        }
    }

    fun setVibrateOnBlock(enabled: Boolean) {
        if (preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToChangeSettings(
                description = "PIN erforderlich, um die Vibrations-Einstellung zu ändern."
            ) {
                repository.setVibrateOnBlock(enabled)
            }
        } else {
            repository.setVibrateOnBlock(enabled)
        }
    }

    fun setShowToastOnBlock(enabled: Boolean) {
        if (preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToChangeSettings(
                description = "PIN erforderlich, um die Benachrichtigungs-Einstellung zu ändern."
            ) {
                repository.setShowToastOnBlock(enabled)
            }
        } else {
            repository.setShowToastOnBlock(enabled)
        }
    }

    fun clearHistory() {
        if (preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToChangeSettings(
                description = "PIN erforderlich, um den Verlauf zu löschen."
            ) {
                viewModelScope.launch {
                    repository.clearHistory()
                }
            }
        } else {
            viewModelScope.launch {
                repository.clearHistory()
            }
        }
    }

    // PIN Setup & Management functions
    fun openSetPinDialog() {
        _pinDialogAction.value = PinDialogAction.SetNewPin
    }

    fun openChangePinDialog() {
        _pinDialogAction.value = PinDialogAction.ChangePin
    }

    fun openRemovePinDialog() {
        _pinDialogAction.value = PinDialogAction.RemovePin
    }

    fun dismissPinDialog() {
        _pinDialogAction.value = null
    }

    fun validatePin(pin: String): Boolean {
        return repository.validatePin(pin)
    }

    fun setPin(pin: String) {
        repository.setPin(pin)
    }

    fun removePin() {
        repository.removePin()
    }

    // Daily Timer Controls
    fun toggleDailyTimer(enabled: Boolean) {
        if (preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToChangeSettings(
                description = if (enabled) "PIN erforderlich, um das tägliche Zeitlimit einzuschalten."
                else "PIN erforderlich, um das tägliche Zeitlimit auszuschalten."
            ) {
                repository.setDailyTimerEnabled(enabled)
            }
        } else {
            repository.setDailyTimerEnabled(enabled)
        }
    }

    fun setDailyLimitMinutes(minutes: Int) {
        if (preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToChangeSettings(
                description = "PIN erforderlich, um das Minutenlimit anzupassen."
            ) {
                repository.setDailyLimitMinutes(minutes)
            }
        } else {
            repository.setDailyLimitMinutes(minutes)
        }
    }

    fun resetDailyTimer() {
        if (preferences.settings.value.isPinEnabled) {
            _pinDialogAction.value = PinDialogAction.VerifyToChangeSettings(
                description = "PIN erforderlich, um das tägliche Zeitlimit zurückzusetzen."
            ) {
                repository.resetDailyUsedSeconds()
            }
        } else {
            repository.resetDailyUsedSeconds()
        }
    }

    fun simulateTimerExpiredNotification(context: Context) {
        val limit = preferences.settings.value.dailyLimitMinutes
        com.example.service.NotificationHelper.showTimeExpiredNotification(context, limit)
    }

    fun setShowSetupDialog(show: Boolean) {
        _showSetupDialog.value = show
    }

    fun simulateShortsDetection(context: Context) {
        viewModelScope.launch {
            val settings = preferences.settings.value
            val actionText = when (settings.youtubeAction) {
                BlockAction.REDIRECT_HOME -> "Zur YouTube-Startseite weitergeleitet"
                BlockAction.PRESS_BACK -> "Zurück-Taste ausgeführt"
                BlockAction.CLOSE_APP -> "App geschlossen (Homescreen)"
            }

            repository.recordBlockedEvent(
                appName = "YouTube App (Test)",
                packageName = "com.google.android.youtube",
                reason = "Test: YouTube Shorts Erkennung simuliert",
                actionTaken = actionText
            )

            if (settings.vibrateOnBlock) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            v?.vibrate(70)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            if (settings.showToastOnBlock) {
                Toast.makeText(
                    context,
                    "🚫 Shorts Test blockiert: $actionText",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BlockerViewModel(application) as T
            }
        }
    }
}
