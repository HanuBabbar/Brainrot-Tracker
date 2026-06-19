package com.example.brainrottracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the dataStore extension on Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettings(private val context: Context) {

    companion object {
        private val AUTH_MODE_KEY = stringPreferencesKey("auth_mode")
    }

    suspend fun setAuthMode(mode: AuthMode) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_MODE_KEY] = mode.name
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
}
