package com.meita.snapshelf.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.snapShelfDataStore by preferencesDataStore(name = "snapshelf_settings")

class UserPreferences(private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.snapShelfDataStore.data.map { prefs ->
        prefs[themeModeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.snapShelfDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }
}

