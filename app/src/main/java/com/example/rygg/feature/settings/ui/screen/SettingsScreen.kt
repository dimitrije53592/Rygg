package com.example.rygg.feature.settings.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.theme.ThemeMode
import com.example.rygg.feature.settings.domain.AppLanguage

@Composable
fun SettingsScreen(params: SettingsScreenParams) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Text(text = stringResource(R.string.settings_theme_title), style = RyggTheme.typography.titleMedium)
        ThemeMode.entries.forEach { mode ->
            OptionRow(
                label = stringResource(themeModeLabel(mode)),
                selected = params.uiState.themeMode == mode,
                onSelect = { params.onThemeModeSelected(mode) }
            )
        }

        Text(text = stringResource(R.string.settings_language_title), style = RyggTheme.typography.titleMedium)
        AppLanguage.entries.forEach { language ->
            OptionRow(
                label = stringResource(language.labelRes),
                selected = params.uiState.selectedLanguage == language,
                onSelect = { params.onLanguageSelected(language) }
            )
        }
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
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH
)

data class SettingsScreenParams(
    val uiState: SettingsUiState,
    val onThemeModeSelected: (ThemeMode) -> Unit,
    val onLanguageSelected: (AppLanguage) -> Unit
)
