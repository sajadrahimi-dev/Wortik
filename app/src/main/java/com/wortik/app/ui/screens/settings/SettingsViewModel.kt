package com.wortik.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortik.app.data.local.AppPalette
import com.wortik.app.data.local.ThemeMode
import com.wortik.app.data.local.UserPreferences
import com.wortik.app.data.local.UserSettings
import com.wortik.app.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferences,
    repository: DictionaryRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings?> = preferences.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val wordCount: StateFlow<Int> = repository.observeWordCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDynamicColor(enabled)
        }
    }

    fun setPalette(palette: AppPalette) {
        viewModelScope.launch {
            preferences.setPalette(palette)
        }
    }

    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAmoledBlack(enabled)
        }
    }
}
