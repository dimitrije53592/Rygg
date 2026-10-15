package com.example.rygg.feature.settings.ui.wrapper

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.core.location.LocationPermission
import com.example.rygg.core.location.locationPermission
import com.example.rygg.core.ui.utils.notificationsEnabled
import com.example.rygg.core.ui.utils.openAppNotificationSettings
import com.example.rygg.core.ui.utils.openAppSettings
import com.example.rygg.feature.settings.domain.PermissionStatus
import com.example.rygg.feature.settings.ui.screen.SettingsScreen
import com.example.rygg.feature.settings.ui.screen.SettingsScreenParams
import com.example.rygg.feature.settings.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsWrapper(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.onPermissionsChecked(
            location = when (context.locationPermission()) {
                LocationPermission.PRECISE -> PermissionStatus.ALLOWED
                LocationPermission.APPROXIMATE -> PermissionStatus.APPROXIMATE
                LocationPermission.NONE -> PermissionStatus.DENIED
            },
            notifications = if (context.notificationsEnabled()) PermissionStatus.ALLOWED else PermissionStatus.DENIED
        )
        onPauseOrDispose { }
    }

    SettingsScreen(
        params = SettingsScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onThemeModeSelected = viewModel::setThemeMode,
            onLanguageSelected = { language ->
                viewModel.setLanguage(language)
                activity?.recreate()
            },
            onMeasurementSystemSelected = viewModel::setMeasurementSystem,
            onSyncEnabledChanged = viewModel::setSyncEnabled,
            onSyncWifiOnlyChanged = viewModel::setSyncWifiOnly,
            onRecordingResolutionSelected = viewModel::setRecordingResolution,
            onManageLocationPermission = context::openAppSettings,
            onManageNotificationPermission = context::openAppNotificationSettings
        )
    )
}
