package com.example.rygg.feature.map.ui.wrapper

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.rygg.feature.map.ui.screen.MapScreen
import com.example.rygg.feature.map.ui.screen.MapScreenParams
import com.example.rygg.feature.map.ui.viewmodel.MapViewModel

@Composable
fun MapWrapper(
    viewModel: MapViewModel = hiltViewModel()
) {
    MapScreen(
        params = MapScreenParams(
            styleUrl = viewModel.uiState.styleUrl
        )
    )
}
