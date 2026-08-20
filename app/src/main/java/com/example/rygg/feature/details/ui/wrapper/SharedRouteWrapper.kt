package com.example.rygg.feature.details.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.details.ui.screen.DetailsMode
import com.example.rygg.feature.details.ui.screen.DetailsScreen
import com.example.rygg.feature.details.ui.screen.DetailsScreenParams
import com.example.rygg.feature.details.ui.viewmodel.SharedRouteViewModel

@Composable
fun SharedRouteWrapper(
    onNavigateBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: SharedRouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedEntryId) {
        uiState.savedEntryId?.let(onSaved)
    }

    DetailsScreen(
        params = DetailsScreenParams(
            uiState = uiState.details,
            onNavigateBack = onNavigateBack,
            mode = DetailsMode.SharedPreview(
                isSaving = uiState.isSaving,
                onSaveCopy = { viewModel.onSaveCopy() }
            )
        )
    )
}
