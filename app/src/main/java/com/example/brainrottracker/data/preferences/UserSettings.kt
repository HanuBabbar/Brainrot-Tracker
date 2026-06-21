package com.example.brainrottracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
}
