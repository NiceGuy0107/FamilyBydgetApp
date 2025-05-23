package com.example.familybudget.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_themes")

class ThemePreferences(private val context: Context) {
    companion object {
        private const val THEME_KEY_PREFIX = "theme_"
    }

    suspend fun getTheme(username: String): String {
        val key = stringPreferencesKey(THEME_KEY_PREFIX + username)
        return context.dataStore.data
            .map { it[key] ?: "light" }
            .first()
    }

    suspend fun saveTheme(username: String, theme: String) {
        val key = stringPreferencesKey(THEME_KEY_PREFIX + username)
        context.dataStore.edit { prefs ->
            prefs[key] = theme
        }
    }
}
