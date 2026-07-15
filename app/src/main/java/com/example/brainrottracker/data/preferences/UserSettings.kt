package com.example.brainrottracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the dataStore extension on Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettings(private val context: Context) {

    companion object {
        private val AUTH_MODE_KEY = stringPreferencesKey("auth_mode")
        private val DAILY_LIMIT_KEY = intPreferencesKey("daily_limit")
        private val LAST_NOTIFIED_DATE_KEY = stringPreferencesKey("last_notified_date")
        private val CPU_MODE_KEY = stringPreferencesKey("cpu_mode")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val FRIEND_CODE_KEY = stringPreferencesKey("friend_code")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val PERSISTENT_NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("persistent_notification_enabled")
        private val STRICT_MODE_ENABLED_KEY = booleanPreferencesKey("strict_mode_enabled")
        private val NOTIFIED_FRIEND_REQUESTS_KEY = stringSetPreferencesKey("notified_friend_requests")
    }

    suspend fun setAuthMode(mode: AuthMode) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_MODE_KEY] = mode.name
        }
    }

    suspend fun setDailyLimit(limit: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_LIMIT_KEY] = limit
        }
    }

    suspend fun setLastNotifiedDate(date: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_NOTIFIED_DATE_KEY] = date
        }
    }

    suspend fun setCPUMode(mode: CPUMode) {
        context.dataStore.edit { prefs ->
            prefs[CPU_MODE_KEY] = mode.name
        }
    }

    suspend fun setUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId != null) {
                prefs[USER_ID_KEY] = userId
            } else {
                prefs.remove(USER_ID_KEY)
            }
        }
    }

    suspend fun setFriendCode(code: String?) {
        context.dataStore.edit { prefs ->
            if (code != null) {
                prefs[FRIEND_CODE_KEY] = code
            } else {
                prefs.remove(FRIEND_CODE_KEY)
            }
        }
    }

    suspend fun setUserName(name: String?) {
        context.dataStore.edit { prefs ->
            if (name != null) {
                prefs[USER_NAME_KEY] = name
            } else {
                prefs.remove(USER_NAME_KEY)
            }
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[VIBRATION_ENABLED_KEY] = enabled
        }
    }

    suspend fun setPersistentNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PERSISTENT_NOTIFICATION_ENABLED_KEY] = enabled
        }
    }

    suspend fun setStrictModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[STRICT_MODE_ENABLED_KEY] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun addNotifiedFriendRequest(friendId: String) {
        context.dataStore.edit { prefs ->
            val currentSet = prefs[NOTIFIED_FRIEND_REQUESTS_KEY] ?: emptySet()
            prefs[NOTIFIED_FRIEND_REQUESTS_KEY] = currentSet + friendId
        }
    }

    suspend fun clearNotifiedFriendRequests() {
        context.dataStore.edit { prefs ->
            prefs.remove(NOTIFIED_FRIEND_REQUESTS_KEY)
        }
    }

    val authMode: Flow<AuthMode> = context.dataStore.data.map { prefs ->
        val stored = prefs[AUTH_MODE_KEY]
        try {
            stored?.let { AuthMode.valueOf(it) } ?: AuthMode.UNKNOWN
        } catch (e: IllegalArgumentException) {
            AuthMode.UNKNOWN
        }
    }

    val dailyLimit: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_LIMIT_KEY] ?: 100 // Default limit 100
    }

    val lastNotifiedDate: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_NOTIFIED_DATE_KEY]
    }

    val cpuMode: Flow<CPUMode> = context.dataStore.data.map { prefs ->
        val stored = prefs[CPU_MODE_KEY]
        try {
            stored?.let { CPUMode.valueOf(it) } ?: CPUMode.MEDIUM
        } catch (e: IllegalArgumentException) {
            CPUMode.MEDIUM
        }
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    val friendCode: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[FRIEND_CODE_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY]
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[VIBRATION_ENABLED_KEY] ?: true // Default to true
    }

    val persistentNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PERSISTENT_NOTIFICATION_ENABLED_KEY] ?: false // Default to false
    }

    val strictModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[STRICT_MODE_ENABLED_KEY] ?: false // Default to false
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val stored = prefs[THEME_MODE_KEY]
        try {
            stored?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    val notifiedFriendRequests: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[NOTIFIED_FRIEND_REQUESTS_KEY] ?: emptySet()
    }
}
