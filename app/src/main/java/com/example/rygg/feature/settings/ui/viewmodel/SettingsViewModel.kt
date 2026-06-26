package com.example.rygg.feature.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.ui.theme.ThemeMode
import com.example.rygg.feature.settings.data.SettingsRepository
import com.example.rygg.feature.settings.domain.AppLanguage
import com.example.rygg.feature.settings.ui.screen.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val selectedLanguage = MutableStateFlow(settingsRepository.currentLanguage())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.themeMode,
        selectedLanguage
    ) { themeMode, language ->
        SettingsUiState(themeMode = themeMode, selectedLanguage = language)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(selectedLanguage = selectedLanguage.value)
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        selectedLanguage.value = language
        settingsRepository.setLanguage(language)
    }
}
