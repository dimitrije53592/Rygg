package com.dimitrijeilic.rygg.feature.map.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.feature.map.ui.screen.MapScreen
import com.dimitrijeilic.rygg.feature.map.ui.screen.MapScreenParams
import com.dimitrijeilic.rygg.feature.map.ui.viewmodel.MapViewModel

@Composable
fun MapWrapper(
    onStartFollow: (Long) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MapScreen(
        params = MapScreenParams(
            styleUrl = uiState.styleUrl,
            routes = uiState.routes,
            focusEntryId = uiState.focusEntryId,
            onFocusRoute = viewModel::onFocusRoute,
            onStartFollow = onStartFollow,
            onOpenProfile = onOpenProfile
        )
    )
}
