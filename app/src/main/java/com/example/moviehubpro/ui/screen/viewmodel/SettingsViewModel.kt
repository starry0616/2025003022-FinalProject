package com.example.moviehubpro.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviehubpro.data.datastore.AppDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val appDataStore: AppDataStore) : ViewModel() {

    // 共享全局主题状态，超时 5s 停止以节省资源
    val isDarkMode: StateFlow<Boolean> = appDataStore.darkModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            appDataStore.setDarkMode(isDark)
        }
    }
}
