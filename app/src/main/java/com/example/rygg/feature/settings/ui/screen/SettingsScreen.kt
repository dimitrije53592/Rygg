package com.example.rygg.feature.settings.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggCard
import com.example.rygg.core.ui.components.RyggSwitchRow
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.theme.ThemeMode
import com.example.rygg.feature.settings.domain.AppLanguage

@Composable
fun SettingsScreen(params: SettingsScreenParams) {
    Scaffold(
        topBar = {
            RyggTopAppBar(title = stringResource(R.string.settings_title), actions = {})
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
            SettingsSection(title = stringResource(R.string.settings_theme_title)) {
                ThemeMode.entries.forEach { mode ->
                    OptionRow(
                        label = stringResource(themeModeLabel(mode)),
                        selected = params.uiState.themeMode == mode,
                        onSelect = { params.onThemeModeSelected(mode) }
                    )
                }
            }

            SettingsSection(title = stringResource(R.string.settings_language_title)) {
                AppLanguage.entries.forEach { language ->
                    OptionRow(
                        label = stringResource(language.labelRes),
                        selected = params.uiState.selectedLanguage == language,
                        onSelect = { params.onLanguageSelected(language) }
                    )
                }
            }

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
                Text(
                    text = stringResource(R.string.settings_sync_description),
                    style = RyggTheme.typography.bodySmall,
                    color = RyggTheme.getColor(RyggColor.TextSecondary),
                    modifier = Modifier.padding(top = RyggTheme.dimens.commonContentPadding4)
                )
            }
        }
    }
}

// A titled group: a subtle section label above a card holding the section's rows.
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
private fun OptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(vertical = RyggTheme.dimens.commonContentPadding8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, style = RyggTheme.typography.bodyLarge)
    }
}

@StringRes
private fun themeModeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val syncEnabled: Boolean = true,
    val syncWifiOnly: Boolean = true
)

data class SettingsScreenParams(
    val uiState: SettingsUiState,
    val onThemeModeSelected: (ThemeMode) -> Unit,
    val onLanguageSelected: (AppLanguage) -> Unit,
    val onSyncEnabledChanged: (Boolean) -> Unit,
    val onSyncWifiOnlyChanged: (Boolean) -> Unit
)
