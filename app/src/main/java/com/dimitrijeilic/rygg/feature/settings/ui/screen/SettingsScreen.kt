package com.dimitrijeilic.rygg.feature.settings.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggCard
import com.dimitrijeilic.rygg.core.ui.components.RyggRadioRow
import com.dimitrijeilic.rygg.core.ui.components.RyggSegmentedControl
import com.dimitrijeilic.rygg.core.ui.components.RyggSwitchRow
import com.dimitrijeilic.rygg.core.ui.components.RyggTopAppBar
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.theme.ThemeMode
import com.dimitrijeilic.rygg.core.ui.utils.rememberRyggHaptics
import com.dimitrijeilic.rygg.core.units.MeasurementSystem
import com.dimitrijeilic.rygg.feature.map.domain.OffRouteThreshold
import com.dimitrijeilic.rygg.feature.record.domain.RecordingResolution
import com.dimitrijeilic.rygg.feature.settings.domain.AppLanguage
import com.dimitrijeilic.rygg.feature.settings.domain.PermissionStatus
import com.dimitrijeilic.rygg.feature.settings.ui.components.SettingsLinkRow
import com.dimitrijeilic.rygg.feature.settings.ui.components.SettingsStatusRow

// Short choices (theme, language, units) are segmented controls, so each setting is one row
// instead of a stack of radio buttons; only resolution keeps radios, because each option needs
// a line of explanation.
@Composable
fun SettingsScreen(params: SettingsScreenParams) {
    Scaffold(
        containerColor = RyggTheme.getColor(RyggColor.SurfaceDim),
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.settings_title),
                onNavigateBack = params.onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)
        ) {
            AppearanceSection(params)
            LanguageRegionSection(params)
            RecordingSection(params)
            NavigationSection(params)
            SyncSection(params)
            PermissionsSection(params)
            AboutSection(params)
            AboutFooter(versionName = params.uiState.appVersion)
        }
    }
}

@Composable
private fun AppearanceSection(params: SettingsScreenParams) {
    SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
        SettingLabel(text = stringResource(R.string.settings_theme_title))
        RyggSegmentedControl(
            options = ThemeMode.entries,
            selected = params.uiState.themeMode,
            onSelect = params.onThemeModeSelected,
            label = { stringResource(themeModeLabel(it)) }
        )
    }
}

@Composable
private fun LanguageRegionSection(params: SettingsScreenParams) {
    SettingsSection(title = stringResource(R.string.settings_section_language_region)) {
        SettingLabel(text = stringResource(R.string.settings_language_title))
        RyggSegmentedControl(
            options = AppLanguage.entries,
            selected = params.uiState.selectedLanguage,
            onSelect = params.onLanguageSelected,
            label = { stringResource(it.labelRes) }
        )
        SectionDivider()
        SettingLabel(text = stringResource(R.string.settings_units_title))
        RyggSegmentedControl(
            options = MeasurementSystem.entries,
            selected = params.uiState.measurementSystem,
            onSelect = params.onMeasurementSystemSelected,
            label = { stringResource(unitsLabel(it)) }
        )
    }
}

@Composable
private fun RecordingSection(params: SettingsScreenParams) {
    val haptics = rememberRyggHaptics()
    SettingsSection(
        title = stringResource(R.string.settings_recording_title),
        footer = stringResource(R.string.settings_recording_description)
    ) {
        RecordingResolution.entries.forEach { resolution ->
            RyggRadioRow(
                label = stringResource(resolution.labelRes),
                description = stringResource(resolution.descriptionRes),
                selected = params.uiState.recordingResolution == resolution,
                onSelect = {
                    if (params.uiState.recordingResolution != resolution) haptics.tick()
                    params.onRecordingResolutionSelected(resolution)
                }
            )
        }
    }
}

@Composable
private fun NavigationSection(params: SettingsScreenParams) {
    val haptics = rememberRyggHaptics()
    SettingsSection(
        title = stringResource(R.string.settings_navigation_title),
        footer = stringResource(R.string.settings_off_route_description)
    ) {
        RyggSwitchRow(
            label = stringResource(R.string.settings_off_route_alerts),
            checked = params.uiState.offRouteAlertsEnabled,
            leadingIcon = Icons.Outlined.NotificationsActive,
            onCheckedChange = {
                haptics.toggle(it)
                params.onOffRouteAlertsChanged(it)
            }
        )
        SectionDivider()
        SettingLabel(text = stringResource(R.string.settings_off_route_threshold))
        RyggSegmentedControl(
            options = OffRouteThreshold.entries,
            selected = params.uiState.offRouteThreshold,
            onSelect = params.onOffRouteThresholdSelected,
            label = { stringResource(it.labelRes) }
        )
    }
}

@Composable
private fun SyncSection(params: SettingsScreenParams) {
    val haptics = rememberRyggHaptics()
    SettingsSection(
        title = stringResource(R.string.settings_sync_title),
        footer = stringResource(R.string.settings_sync_description)
    ) {
        RyggSwitchRow(
            label = stringResource(R.string.settings_sync_enabled),
            checked = params.uiState.syncEnabled,
            leadingIcon = Icons.Outlined.CloudUpload,
            onCheckedChange = {
                haptics.toggle(it)
                params.onSyncEnabledChanged(it)
            }
        )
        SectionDivider(inset = true)
        RyggSwitchRow(
            label = stringResource(R.string.settings_sync_wifi_only),
            checked = params.uiState.syncWifiOnly,
            leadingIcon = Icons.Outlined.Wifi,
            onCheckedChange = {
                haptics.toggle(it)
                params.onSyncWifiOnlyChanged(it)
            },
            enabled = params.uiState.syncEnabled
        )
        SectionDivider(inset = true)
        RyggSwitchRow(
            label = stringResource(R.string.settings_sync_photos),
            checked = params.uiState.syncPhotos,
            leadingIcon = Icons.Outlined.PhotoLibrary,
            onCheckedChange = {
                haptics.toggle(it)
                params.onSyncPhotosChanged(it)
            },
            enabled = params.uiState.syncEnabled
        )
    }
}

@Composable
private fun PermissionsSection(params: SettingsScreenParams) {
    SettingsSection(
        title = stringResource(R.string.settings_permissions_title),
        footer = stringResource(R.string.settings_permissions_description)
    ) {
        SettingsStatusRow(
            label = stringResource(R.string.settings_permission_location),
            status = stringResource(permissionStatusLabel(params.uiState.locationPermission)),
            isOk = params.uiState.locationPermission == PermissionStatus.ALLOWED,
            actionLabel = stringResource(R.string.settings_permission_manage),
            onAction = params.onManageLocationPermission,
            leadingIcon = Icons.Outlined.LocationOn
        )
        SectionDivider(inset = true)
        SettingsStatusRow(
            label = stringResource(R.string.settings_permission_notifications),
            status = stringResource(permissionStatusLabel(params.uiState.notificationPermission)),
            isOk = params.uiState.notificationPermission == PermissionStatus.ALLOWED,
            actionLabel = stringResource(R.string.settings_permission_manage),
            onAction = params.onManageNotificationPermission,
            leadingIcon = Icons.Outlined.Notifications
        )
    }
}

// Privacy and terms live on the website so Play Console and the app link to the same page.
@Composable
private fun AboutSection(params: SettingsScreenParams) {
    SettingsSection(title = stringResource(R.string.settings_section_about)) {
        SettingsLinkRow(
            label = stringResource(R.string.settings_privacy_policy),
            leadingIcon = Icons.Outlined.Policy,
            trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
            onClick = params.onOpenPrivacyPolicy
        )
        SectionDivider(inset = true)
        SettingsLinkRow(
            label = stringResource(R.string.settings_terms),
            leadingIcon = Icons.Outlined.Gavel,
            trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
            onClick = params.onOpenTerms
        )
        SectionDivider(inset = true)
        SettingsLinkRow(
            label = stringResource(R.string.settings_licenses),
            leadingIcon = Icons.Outlined.Description,
            onClick = params.onOpenLicenses
        )
    }
}

@Composable
private fun AboutFooter(versionName: String) {
    if (versionName.isEmpty()) return
    Text(
        text = stringResource(R.string.settings_about_version, versionName),
        style = RyggTheme.typography.bodySmall,
        color = RyggTheme.getColor(RyggColor.TextSecondary),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// Grouped-list section: tracked header above the card, explanatory footer below it.
@Composable
private fun SettingsSection(
    title: String,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
        Text(
            text = title.uppercase(),
            style = RyggTheme.textStyles.trackedLabel,
            color = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier.padding(start = RyggTheme.dimens.commonContentPadding4)
        )
        RyggCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8),
                content = content
            )
        }
        footer?.let {
            Text(
                text = it,
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.padding(horizontal = RyggTheme.dimens.commonContentPadding4)
            )
        }
    }
}

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = RyggTheme.typography.titleSmall,
        color = RyggTheme.getColor(RyggColor.TextPrimary)
    )
}

// Inset dividers start where row text starts, past the icon badge.
@Composable
private fun SectionDivider(inset: Boolean = false) {
    HorizontalDivider(
        color = RyggTheme.getColor(RyggColor.Outline),
        modifier = Modifier.padding(
            start = if (inset) RyggTheme.dimens.listDividerInset else RyggTheme.dimens.zeroPadding,
            top = RyggTheme.dimens.commonContentPadding4,
            bottom = RyggTheme.dimens.commonContentPadding4
        )
    )
}

@StringRes
private fun themeModeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system_short
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

@StringRes
private fun unitsLabel(system: MeasurementSystem): Int = when (system) {
    MeasurementSystem.METRIC -> R.string.settings_units_metric_short
    MeasurementSystem.IMPERIAL -> R.string.settings_units_imperial_short
}

@StringRes
private fun permissionStatusLabel(status: PermissionStatus): Int = when (status) {
    PermissionStatus.ALLOWED -> R.string.settings_permission_allowed
    PermissionStatus.APPROXIMATE -> R.string.settings_permission_approximate
    PermissionStatus.DENIED -> R.string.settings_permission_denied
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    RyggTheme {
        SettingsScreen(
            params = SettingsScreenParams(
                uiState = SettingsUiState(
                    measurementSystem = MeasurementSystem.IMPERIAL,
                    locationPermission = PermissionStatus.APPROXIMATE,
                    notificationPermission = PermissionStatus.DENIED,
                    appVersion = "1.0"
                ),
                onNavigateBack = {},
                onThemeModeSelected = {},
                onLanguageSelected = {},
                onMeasurementSystemSelected = {},
                onSyncEnabledChanged = {},
                onSyncWifiOnlyChanged = {},
                onSyncPhotosChanged = {},
                onRecordingResolutionSelected = {},
                onOffRouteAlertsChanged = {},
                onOffRouteThresholdSelected = {},
                onManageLocationPermission = {},
                onManageNotificationPermission = {},
                onOpenPrivacyPolicy = {},
                onOpenTerms = {},
                onOpenLicenses = {}
            )
        )
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val measurementSystem: MeasurementSystem = MeasurementSystem.METRIC,
    val syncEnabled: Boolean = true,
    val syncWifiOnly: Boolean = true,
    val syncPhotos: Boolean = true,
    val recordingResolution: RecordingResolution = RecordingResolution.BALANCED,
    val offRouteAlertsEnabled: Boolean = true,
    val offRouteThreshold: OffRouteThreshold = OffRouteThreshold.DEFAULT,
    val locationPermission: PermissionStatus = PermissionStatus.DENIED,
    val notificationPermission: PermissionStatus = PermissionStatus.DENIED,
    val appVersion: String = ""
)

data class SettingsScreenParams(
    val uiState: SettingsUiState,
    val onNavigateBack: () -> Unit,
    val onThemeModeSelected: (ThemeMode) -> Unit,
    val onLanguageSelected: (AppLanguage) -> Unit,
    val onMeasurementSystemSelected: (MeasurementSystem) -> Unit,
    val onSyncEnabledChanged: (Boolean) -> Unit,
    val onSyncWifiOnlyChanged: (Boolean) -> Unit,
    val onSyncPhotosChanged: (Boolean) -> Unit,
    val onRecordingResolutionSelected: (RecordingResolution) -> Unit,
    val onOffRouteAlertsChanged: (Boolean) -> Unit,
    val onOffRouteThresholdSelected: (OffRouteThreshold) -> Unit,
    val onManageLocationPermission: () -> Unit,
    val onManageNotificationPermission: () -> Unit,
    val onOpenPrivacyPolicy: () -> Unit,
    val onOpenTerms: () -> Unit,
    val onOpenLicenses: () -> Unit
)
