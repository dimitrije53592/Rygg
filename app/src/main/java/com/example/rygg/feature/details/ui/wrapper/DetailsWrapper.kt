package com.example.rygg.feature.details.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.details.ui.screen.DetailsScreen
import com.example.rygg.feature.details.ui.screen.DetailsScreenParams
import com.example.rygg.feature.details.ui.viewmodel.DetailsViewModel

@Composable
fun DetailsWrapper(
    onNavigateBack: () -> Unit,
    onViewOnMap: (Long) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailsScreen(
        params = DetailsScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onViewOnMap = onViewOnMap,
            onToggleFavorite = { viewModel.onToggleFavorite() },
            onDelete = {
                viewModel.onDelete()
                onNavigateBack()
            }
        )
    )
}
