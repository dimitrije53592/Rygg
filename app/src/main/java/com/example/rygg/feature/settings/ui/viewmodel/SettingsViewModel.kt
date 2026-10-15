package com.example.rygg.feature.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rygg.core.ui.theme.ThemeMode
import com.example.rygg.feature.record.domain.RecordingResolution
import com.example.rygg.feature.settings.data.SettingsRepository
import com.example.rygg.feature.settings.domain.AppLanguage
import com.example.rygg.feature.settings.domain.PermissionStatus
import com.example.rygg.feature.settings.ui.screen.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _selectedLanguage = MutableStateFlow(settingsRepository.currentLanguage())
    private val permissions = MutableStateFlow(Permissions())
    private val appVersion = settingsRepository.appVersionName()

    private val appearanceAndRegion = combine(
        settingsRepository.themeMode,
        _selectedLanguage
    ) { themeMode, language ->
        AppearanceAndRegion(themeMode, language)
    }

    private val recordingAndSync = combine(
        settingsRepository.recordingResolution,
        settingsRepository.syncEnabled,
        settingsRepository.syncWifiOnly
    ) { recordingResolution, syncEnabled, syncWifiOnly ->
        RecordingAndSync(recordingResolution, syncEnabled, syncWifiOnly)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        appearanceAndRegion,
        recordingAndSync,
        permissions
    ) { region, recording, permissions ->
        SettingsUiState(
            themeMode = region.themeMode,
            selectedLanguage = region.language,
            syncEnabled = recording.syncEnabled,
            syncWifiOnly = recording.syncWifiOnly,
            recordingResolution = recording.recordingResolution,
            locationPermission = permissions.location,
            notificationPermission = permissions.notifications,
            appVersion = appVersion
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(selectedLanguage = _selectedLanguage.value, appVersion = appVersion)
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
        settingsRepository.setLanguage(language)
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSyncEnabled(enabled) }
    }

    fun setSyncWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch { settingsRepository.setSyncWifiOnly(wifiOnly) }
    }

    fun setRecordingResolution(resolution: RecordingResolution) {
        viewModelScope.launch { settingsRepository.setRecordingResolution(resolution) }
    }

    fun onPermissionsChecked(location: PermissionStatus, notifications: PermissionStatus) {
        permissions.update { it.copy(location = location, notifications = notifications) }
    }
}

private data class AppearanceAndRegion(
    val themeMode: ThemeMode,
    val language: AppLanguage
)

private data class RecordingAndSync(
    val recordingResolution: RecordingResolution,
    val syncEnabled: Boolean,
    val syncWifiOnly: Boolean
)

private data class Permissions(
    val location: PermissionStatus = PermissionStatus.DENIED,
    val notifications: PermissionStatus = PermissionStatus.DENIED
)
