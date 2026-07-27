package com.example.rygg.feature.map.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.map.ui.screen.MapScreen
import com.example.rygg.feature.map.ui.screen.MapScreenParams
import com.example.rygg.feature.map.ui.viewmodel.MapViewModel

@Composable
fun MapWrapper(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MapScreen(
        params = MapScreenParams(
            styleUrl = uiState.styleUrl,
            routes = uiState.routes,
            focusEntryId = uiState.focusEntryId
        )
    )
}
