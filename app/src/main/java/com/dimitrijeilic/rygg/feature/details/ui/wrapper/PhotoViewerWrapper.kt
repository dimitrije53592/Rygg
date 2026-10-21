package com.dimitrijeilic.rygg.feature.details.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.feature.details.ui.screen.PhotoViewerScreen
import com.dimitrijeilic.rygg.feature.details.ui.screen.PhotoViewerScreenParams
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.PhotoViewerLoadingState
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.PhotoViewerViewModel

@Composable
fun PhotoViewerWrapper(
    onNavigateBack: () -> Unit,
    viewModel: PhotoViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Deleting the last photo leaves nothing to show.
    val loadingState = uiState.loadingState
    LaunchedEffect(loadingState) {
        if (loadingState is PhotoViewerLoadingState.Loaded && loadingState.images.isEmpty()) onNavigateBack()
    }

    PhotoViewerScreen(
        params = PhotoViewerScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onSetCover = viewModel::onSetCover,
            onDeletePhoto = viewModel::onDeletePhoto
        )
    )
}
