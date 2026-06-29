package com.example.moviehubpro.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class AppDataStore(private val context: Context) {

    companion object {
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val SEARCH_HISTORY = stringSetPreferencesKey("search_history")
    }

    // 主题流
    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = isDark }
    }

    // 搜索历史流
    val searchHistoryFlow: Flow<List<String>> = context.dataStore.data.map { 
        it[SEARCH_HISTORY]?.toList() ?: emptyList() 
    }

    suspend fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = prefs[SEARCH_HISTORY]?.toMutableSet() ?: mutableSetOf()
            val newList = (listOf(query) + current.toList()).take(10).toSet() // 最多存10条
            prefs[SEARCH_HISTORY] = newList
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it.remove(SEARCH_HISTORY) }
    }
}
