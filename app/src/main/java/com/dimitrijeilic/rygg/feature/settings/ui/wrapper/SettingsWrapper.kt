package com.dimitrijeilic.rygg.feature.settings.ui.wrapper

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.core.location.LocationPermission
import com.dimitrijeilic.rygg.core.location.locationPermission
import com.dimitrijeilic.rygg.core.ui.utils.notificationsEnabled
import com.dimitrijeilic.rygg.core.ui.utils.openAppNotificationSettings
import com.dimitrijeilic.rygg.core.ui.utils.openAppSettings
import com.dimitrijeilic.rygg.feature.settings.domain.LegalLinks
import com.dimitrijeilic.rygg.feature.settings.domain.PermissionStatus
import com.dimitrijeilic.rygg.feature.settings.ui.screen.SettingsScreen
import com.dimitrijeilic.rygg.feature.settings.ui.screen.SettingsScreenParams
import com.dimitrijeilic.rygg.feature.settings.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsWrapper(
    onNavigateBack: () -> Unit,
    onOpenLicenses: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

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
            onSyncPhotosChanged = viewModel::setSyncPhotos,
            onRecordingResolutionSelected = viewModel::setRecordingResolution,
            onOffRouteAlertsChanged = viewModel::setOffRouteAlertsEnabled,
            onOffRouteThresholdSelected = viewModel::setOffRouteThreshold,
            onManageLocationPermission = context::openAppSettings,
            onManageNotificationPermission = context::openAppNotificationSettings,
            onOpenPrivacyPolicy = { uriHandler.openUri(LegalLinks.PRIVACY_POLICY) },
            onOpenTerms = { uriHandler.openUri(LegalLinks.TERMS) },
            onOpenLicenses = onOpenLicenses
        )
    )
}
