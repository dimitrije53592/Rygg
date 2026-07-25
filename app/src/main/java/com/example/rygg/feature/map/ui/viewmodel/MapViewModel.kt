package com.example.rygg.feature.map.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rygg.feature.map.domain.MapStyleSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    mapStyleSource: MapStyleSource
) : ViewModel() {
    val uiState = MapUiState(styleUrl = mapStyleSource.outdoorStyleUrl())
}

data class MapUiState(
    val styleUrl: String
)
