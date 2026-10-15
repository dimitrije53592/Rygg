package com.example.rygg.feature.settings.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggCard
import com.example.rygg.core.ui.components.RyggRadioRow
import com.example.rygg.core.ui.components.RyggSwitchRow
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.theme.ThemeMode
import com.example.rygg.core.units.MeasurementSystem
import com.example.rygg.feature.record.domain.RecordingResolution
import com.example.rygg.feature.settings.domain.AppLanguage
import com.example.rygg.feature.settings.domain.PermissionStatus
import com.example.rygg.feature.settings.ui.components.SettingsStatusRow

@Composable
fun SettingsScreen(params: SettingsScreenParams) {
    Scaffold(
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
                .background(RyggTheme.getColor(RyggColor.SurfaceDim))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)
        ) {
            AppearanceSection(params)
            LanguageRegionSection(params)
            RecordingSection(params)
            SyncSection(params)
            PermissionsSection(params)
            AboutFooter(versionName = params.uiState.appVersion)
        }
    }
}

@Composable
private fun AppearanceSection(params: SettingsScreenParams) {
    SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
        SubsectionLabel(text = stringResource(R.string.settings_theme_title))
        ThemeMode.entries.forEach { mode ->
            RyggRadioRow(
                label = stringResource(themeModeLabel(mode)),
                selected = params.uiState.themeMode == mode,
                onSelect = { params.onThemeModeSelected(mode) }
            )
        }
    }
}

@Composable
private fun LanguageRegionSection(params: SettingsScreenParams) {
    SettingsSection(title = stringResource(R.string.settings_section_language_region)) {
        SubsectionLabel(text = stringResource(R.string.settings_language_title))
        AppLanguage.entries.forEach { language ->
            RyggRadioRow(
                label = stringResource(language.labelRes),
                selected = params.uiState.selectedLanguage == language,
                onSelect = { params.onLanguageSelected(language) }
            )
        }
        SectionDivider()
        SubsectionLabel(text = stringResource(R.string.settings_units_title))
        MeasurementSystem.entries.forEach { system ->
            RyggRadioRow(
                label = stringResource(system.labelRes),
                selected = params.uiState.measurementSystem == system,
                onSelect = { params.onMeasurementSystemSelected(system) }
            )
        }
    }
}

@Composable
private fun RecordingSection(params: SettingsScreenParams) {
    SettingsSection(title = stringResource(R.string.settings_recording_title)) {
        RecordingResolution.entries.forEach { resolution ->
            RyggRadioRow(
                label = stringResource(resolution.labelRes),
                selected = params.uiState.recordingResolution == resolution,
                onSelect = { params.onRecordingResolutionSelected(resolution) }
            )
        }
        SectionDescription(text = stringResource(R.string.settings_recording_description))
    }
}

@Composable
private fun SyncSection(params: SettingsScreenParams) {
    SettingsSection(title = stringResource(R.string.settings_sync_title)) {
        RyggSwitchRow(
            label = stringResource(R.string.settings_sync_enabled),
            checked = params.uiState.syncEnabled,
            onCheckedChange = params.onSyncEnabledChanged
        )
        RyggSwitchRow(
            label = stringResource(R.string.settings_sync_wifi_only),
            checked = params.uiState.syncWifiOnly,
            onCheckedChange = params.onSyncWifiOnlyChanged,
            enabled = params.uiState.syncEnabled
        )
        SectionDescription(text = stringResource(R.string.settings_sync_description))
    }
}

@Composable
private fun PermissionsSection(params: SettingsScreenParams) {
    SettingsSection(title = stringResource(R.string.settings_permissions_title)) {
        SettingsStatusRow(
            label = stringResource(R.string.settings_permission_location),
            status = stringResource(permissionStatusLabel(params.uiState.locationPermission)),
            isOk = params.uiState.locationPermission == PermissionStatus.ALLOWED,
            actionLabel = stringResource(R.string.settings_permission_manage),
            onAction = params.onManageLocationPermission
        )
        SettingsStatusRow(
            label = stringResource(R.string.settings_permission_notifications),
            status = stringResource(permissionStatusLabel(params.uiState.notificationPermission)),
            isOk = params.uiState.notificationPermission == PermissionStatus.ALLOWED,
            actionLabel = stringResource(R.string.settings_permission_manage),
            onAction = params.onManageNotificationPermission
        )
        SectionDescription(text = stringResource(R.string.settings_permissions_description))
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

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
        Text(
            text = title,
            style = RyggTheme.typography.titleMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.padding(start = RyggTheme.dimens.commonContentPadding4)
        )
        RyggCard(content = content)
    }
}

@Composable
private fun SubsectionLabel(text: String) {
    Text(
        text = text,
        style = RyggTheme.typography.labelLarge,
        color = RyggTheme.getColor(RyggColor.TextSecondary)
    )
}

@Composable
private fun SectionDescription(text: String) {
    Text(
        text = text,
        style = RyggTheme.typography.bodySmall,
        color = RyggTheme.getColor(RyggColor.TextSecondary),
        modifier = Modifier.padding(top = RyggTheme.dimens.commonContentPadding4)
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        color = RyggTheme.getColor(RyggColor.MutedGray),
        modifier = Modifier.padding(vertical = RyggTheme.dimens.commonContentPadding8)
    )
}

@StringRes
private fun themeModeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
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
                onRecordingResolutionSelected = {},
                onManageLocationPermission = {},
                onManageNotificationPermission = {}
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
    val recordingResolution: RecordingResolution = RecordingResolution.BALANCED,
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
    val onRecordingResolutionSelected: (RecordingResolution) -> Unit,
    val onManageLocationPermission: () -> Unit,
    val onManageNotificationPermission: () -> Unit
)
