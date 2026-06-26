package com.example.rygg.feature.settings.ui.wrapper

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.settings.ui.screen.SettingsScreen
import com.example.rygg.feature.settings.ui.screen.SettingsScreenParams
import com.example.rygg.feature.settings.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsWrapper(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    SettingsScreen(
        params = SettingsScreenParams(
            uiState = uiState,
            onThemeModeSelected = viewModel::setThemeMode,
            onLanguageSelected = { language ->
                viewModel.setLanguage(language)
                activity?.recreate()
            }
        )
    )
}
