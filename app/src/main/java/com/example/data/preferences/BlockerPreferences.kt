package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BlockAction(val key: String, val titleGerman: String, val descriptionGerman: String) {
    REDIRECT_HOME("REDIRECT_HOME", "Zur Startseite springen", "Öffnet den Start-Tab in der YouTube-App"),
    PRESS_BACK("PRESS_BACK", "Zurück-Taste ausführen", "Navigiert sofort zurück zum vorherigen Bildschirm"),
    CLOSE_APP("CLOSE_APP", "App verlassen (Homescreen)", "Schließt die App und wechselt zum Android Startbildschirm")
}

data class BlockerSettings(
    val isProtectionActive: Boolean = true,
    val youtubeAction: BlockAction = BlockAction.REDIRECT_HOME,
    val browserAction: BlockAction = BlockAction.PRESS_BACK,
    val vibrateOnBlock: Boolean = true,
    val showToastOnBlock: Boolean = true,
    val blockShortsShelves: Boolean = true,
    val isPinEnabled: Boolean = false,
    val isDailyTimerEnabled: Boolean = false,
    val dailyLimitMinutes: Int = 15,
    val usedSecondsToday: Long = 0L
) {
    val totalLimitSeconds: Long
        get() = dailyLimitMinutes * 60L

    val remainingSecondsToday: Long
        get() = if (isDailyTimerEnabled) {
            maxOf(0L, totalLimitSeconds - usedSecondsToday)
        } else {
            0L
        }

    val isLimitReached: Boolean
        get() = isDailyTimerEnabled && usedSecondsToday >= totalLimitSeconds

    val usageProgress: Float
        get() = if (!isDailyTimerEnabled || totalLimitSeconds == 0L) {
            0f
        } else {
            (usedSecondsToday.toFloat() / totalLimitSeconds.toFloat()).coerceIn(0f, 1f)
        }
}

class BlockerPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shorts_blocker_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<BlockerSettings> = _settings.asStateFlow()

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun loadSettings(): BlockerSettings {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        var usedSecs = prefs.getLong(KEY_USED_SECONDS, 0L)

        // Reset quota if it's a new day
        if (lastDate != today) {
            usedSecs = 0L
            prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putLong(KEY_USED_SECONDS, 0L)
                .apply()
        }

        val active = prefs.getBoolean(KEY_ACTIVE, true)
        val ytActionStr = prefs.getString(KEY_YT_ACTION, BlockAction.REDIRECT_HOME.name) ?: BlockAction.REDIRECT_HOME.name
        val ytAction = try { BlockAction.valueOf(ytActionStr) } catch (e: Exception) { BlockAction.REDIRECT_HOME }

        val brActionStr = prefs.getString(KEY_BR_ACTION, BlockAction.PRESS_BACK.name) ?: BlockAction.PRESS_BACK.name
        val brAction = try { BlockAction.valueOf(brActionStr) } catch (e: Exception) { BlockAction.PRESS_BACK }

        val vibrate = prefs.getBoolean(KEY_VIBRATE, true)
        val toast = prefs.getBoolean(KEY_TOAST, true)
        val shelves = prefs.getBoolean(KEY_SHELVES, true)
        val pinEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false) && !prefs.getString(KEY_PIN_HASH, "").isNullOrEmpty()

        val timerEnabled = prefs.getBoolean(KEY_TIMER_ENABLED, false)
        val limitMinutes = prefs.getInt(KEY_LIMIT_MINUTES, 15).coerceIn(1, 180)

        return BlockerSettings(
            isProtectionActive = active,
            youtubeAction = ytAction,
            browserAction = brAction,
            vibrateOnBlock = vibrate,
            showToastOnBlock = toast,
            blockShortsShelves = shelves,
            isPinEnabled = pinEnabled,
            isDailyTimerEnabled = timerEnabled,
            dailyLimitMinutes = limitMinutes,
            usedSecondsToday = usedSecs
        )
    }

    fun setProtectionActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_ACTIVE, active).apply()
        _settings.value = _settings.value.copy(isProtectionActive = active)
    }

    fun setYoutubeAction(action: BlockAction) {
        prefs.edit().putString(KEY_YT_ACTION, action.name).apply()
        _settings.value = _settings.value.copy(youtubeAction = action)
    }

    fun setBrowserAction(action: BlockAction) {
        prefs.edit().putString(KEY_BR_ACTION, action.name).apply()
        _settings.value = _settings.value.copy(browserAction = action)
    }

    fun setVibrateOnBlock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply()
        _settings.value = _settings.value.copy(vibrateOnBlock = enabled)
    }

    fun setShowToastOnBlock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TOAST, enabled).apply()
        _settings.value = _settings.value.copy(showToastOnBlock = enabled)
    }

    fun setBlockShortsShelves(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHELVES, enabled).apply()
        _settings.value = _settings.value.copy(blockShortsShelves = enabled)
    }

    fun setDailyTimerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TIMER_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isDailyTimerEnabled = enabled)
    }

    fun setDailyLimitMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 180)
        prefs.edit().putInt(KEY_LIMIT_MINUTES, clamped).apply()
        _settings.value = _settings.value.copy(dailyLimitMinutes = clamped)
    }

    @Synchronized
    fun addUsedShortsSeconds(seconds: Long): Long {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        var current = if (lastDate != today) 0L else prefs.getLong(KEY_USED_SECONDS, 0L)
        current += seconds

        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putLong(KEY_USED_SECONDS, current)
            .apply()

        _settings.value = _settings.value.copy(usedSecondsToday = current)
        return current
    }

    fun resetDailyUsedSeconds() {
        val today = getTodayDateString()
        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putLong(KEY_USED_SECONDS, 0L)
            .apply()
        _settings.value = _settings.value.copy(usedSecondsToday = 0L)
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
        _settings.value = _settings.value.copy(isPinEnabled = true)
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
        _settings.value = _settings.value.copy(isPinEnabled = false)
    }

    fun validatePin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return hashPin(pin) == storedHash
    }

    companion object {
        private const val KEY_ACTIVE = "key_protection_active"
        private const val KEY_YT_ACTION = "key_yt_action"
        private const val KEY_BR_ACTION = "key_br_action"
        private const val KEY_VIBRATE = "key_vibrate"
        private const val KEY_TOAST = "key_toast"
        private const val KEY_SHELVES = "key_shelves"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_ENABLED = "key_pin_enabled"
        private const val KEY_TIMER_ENABLED = "key_timer_enabled"
        private const val KEY_LIMIT_MINUTES = "key_limit_minutes"
        private const val KEY_USED_SECONDS = "key_used_seconds"
        private const val KEY_LAST_DATE = "key_last_date"

        @Volatile
        private var INSTANCE: BlockerPreferences? = null

        fun getInstance(context: Context): BlockerPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = BlockerPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
