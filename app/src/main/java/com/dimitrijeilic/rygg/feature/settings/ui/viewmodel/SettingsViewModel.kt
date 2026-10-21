package com.dimitrijeilic.rygg.feature.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrijeilic.rygg.core.ui.theme.ThemeMode
import com.dimitrijeilic.rygg.core.units.MeasurementSystem
import com.dimitrijeilic.rygg.feature.map.domain.OffRouteThreshold
import com.dimitrijeilic.rygg.feature.record.domain.RecordingResolution
import com.dimitrijeilic.rygg.feature.settings.data.SettingsRepository
import com.dimitrijeilic.rygg.feature.settings.domain.AppLanguage
import com.dimitrijeilic.rygg.feature.settings.domain.PermissionStatus
import com.dimitrijeilic.rygg.feature.settings.ui.screen.SettingsUiState
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
        _selectedLanguage,
        settingsRepository.measurementSystem
    ) { themeMode, language, measurementSystem ->
        AppearanceAndRegion(themeMode, language, measurementSystem)
    }

    private val recordingAndSync = combine(
        settingsRepository.recordingResolution,
        settingsRepository.syncEnabled,
        settingsRepository.syncWifiOnly,
        settingsRepository.syncPhotos
    ) { recordingResolution, syncEnabled, syncWifiOnly, syncPhotos ->
        RecordingAndSync(recordingResolution, syncEnabled, syncWifiOnly, syncPhotos)
    }

    private val navigation = combine(
        settingsRepository.offRouteAlertsEnabled,
        settingsRepository.offRouteThreshold
    ) { alertsEnabled, threshold ->
        Navigation(alertsEnabled, threshold)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        appearanceAndRegion,
        recordingAndSync,
        navigation,
        permissions
    ) { region, recording, navigation, permissions ->
        SettingsUiState(
            themeMode = region.themeMode,
            selectedLanguage = region.language,
            measurementSystem = region.measurementSystem,
            syncEnabled = recording.syncEnabled,
            syncWifiOnly = recording.syncWifiOnly,
            syncPhotos = recording.syncPhotos,
            recordingResolution = recording.recordingResolution,
            offRouteAlertsEnabled = navigation.offRouteAlertsEnabled,
            offRouteThreshold = navigation.offRouteThreshold,
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

    fun setMeasurementSystem(system: MeasurementSystem) {
        viewModelScope.launch { settingsRepository.setMeasurementSystem(system) }
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSyncEnabled(enabled) }
    }

    fun setSyncWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch { settingsRepository.setSyncWifiOnly(wifiOnly) }
    }

    fun setSyncPhotos(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSyncPhotos(enabled) }
    }

    fun setRecordingResolution(resolution: RecordingResolution) {
        viewModelScope.launch { settingsRepository.setRecordingResolution(resolution) }
    }

    fun setOffRouteAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setOffRouteAlertsEnabled(enabled) }
    }

    fun setOffRouteThreshold(threshold: OffRouteThreshold) {
        viewModelScope.launch { settingsRepository.setOffRouteThreshold(threshold) }
    }

    fun onPermissionsChecked(location: PermissionStatus, notifications: PermissionStatus) {
        permissions.update { it.copy(location = location, notifications = notifications) }
    }
}

private data class AppearanceAndRegion(
    val themeMode: ThemeMode,
    val language: AppLanguage,
    val measurementSystem: MeasurementSystem
)

private data class RecordingAndSync(
    val recordingResolution: RecordingResolution,
    val syncEnabled: Boolean,
    val syncWifiOnly: Boolean,
    val syncPhotos: Boolean
)

private data class Navigation(
    val offRouteAlertsEnabled: Boolean,
    val offRouteThreshold: OffRouteThreshold
)

private data class Permissions(
    val location: PermissionStatus = PermissionStatus.DENIED,
    val notifications: PermissionStatus = PermissionStatus.DENIED
)
