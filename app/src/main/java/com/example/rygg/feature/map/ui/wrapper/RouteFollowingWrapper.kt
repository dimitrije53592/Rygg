package com.example.rygg.feature.map.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.map.ui.screen.RouteFollowingScreen
import com.example.rygg.feature.map.ui.screen.RouteFollowingScreenParams
import com.example.rygg.feature.map.ui.viewmodel.RouteFollowingViewModel

@Composable
fun RouteFollowingWrapper(
    onExit: () -> Unit,
    viewModel: RouteFollowingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraAction by viewModel.cameraAction.collectAsStateWithLifecycle(initialValue = null)

    RouteFollowingScreen(
        params = RouteFollowingScreenParams(
            uiState = uiState,
            cameraAction = cameraAction,
            onLocationChange = viewModel::onLocationChange,
            setFreeLook = viewModel::setFreeLook,
            setPendingRebearing = viewModel::setPendingRebearing,
            startPreview = viewModel::startPreview,
            onExit = onExit
        )
    )
}
