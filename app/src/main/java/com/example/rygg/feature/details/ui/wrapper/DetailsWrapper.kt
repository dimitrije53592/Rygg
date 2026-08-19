package com.example.rygg.feature.details.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.core.ui.utils.shareGpxFile
import com.example.rygg.core.ui.utils.shareRouteLink
import com.example.rygg.feature.details.ui.screen.DetailsMode
import com.example.rygg.feature.details.ui.screen.DetailsScreen
import com.example.rygg.feature.details.ui.screen.DetailsScreenParams
import com.example.rygg.feature.details.ui.viewmodel.DetailsLoadingState
import com.example.rygg.feature.details.ui.viewmodel.DetailsViewModel

@Composable
fun DetailsWrapper(
    onNavigateBack: () -> Unit,
    onViewOnMap: (Long) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val routeName = (uiState.loadingState as? DetailsLoadingState.Loaded)?.entry?.name.orEmpty()

    DetailsScreen(
        params = DetailsScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            mode = DetailsMode.View(
                onViewOnMap = onViewOnMap,
                onToggleFavorite = { viewModel.onToggleFavorite() },
                onRename = { viewModel.onRename(it) },
                onDelete = {
                    viewModel.onDelete()
                    onNavigateBack()
                },
                onShareLink = {
                    viewModel.shareLink()?.let { context.shareRouteLink(it, routeName) }
                },
                onShareFile = {
                    viewModel.shareFileUri()?.let { context.shareGpxFile(it, routeName) }
                }
            )
        )
    )
}
