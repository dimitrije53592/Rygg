package com.dimitrijeilic.rygg.feature.settings.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.feature.settings.ui.screen.LicensesScreen
import com.dimitrijeilic.rygg.feature.settings.ui.screen.LicensesScreenParams
import com.dimitrijeilic.rygg.feature.settings.ui.viewmodel.LicensesViewModel

@Composable
fun LicensesWrapper(
    onNavigateBack: () -> Unit,
    viewModel: LicensesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    LicensesScreen(
        params = LicensesScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onToggleExpanded = viewModel::onToggleExpanded,
            onOpenUrl = uriHandler::openUri
        )
    )
}
