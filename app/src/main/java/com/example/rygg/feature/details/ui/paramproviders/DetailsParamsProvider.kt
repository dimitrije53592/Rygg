package com.example.rygg.feature.details.ui.paramproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.feature.details.ui.screen.DetailsScreenParams
import com.example.rygg.feature.details.ui.viewmodel.DetailsLoadingState
import com.example.rygg.feature.details.ui.viewmodel.DetailsUiState
import com.example.rygg.feature.library.ui.paramproviders.previewGpxFileEntry

// One preview frame per loading state the screen's `when` handles.
class DetailsParamsProvider : PreviewParameterProvider<DetailsScreenParams> {
    override val values = sequenceOf(
        detailsParams(DetailsLoadingState.Loading),
        detailsParams(DetailsLoadingState.Error(null)),
        detailsParams(
            DetailsLoadingState.Loaded(
                entry = previewGpxFileEntry(),
                elevationProfile = previewProfile()
            )
        )
    )
}

private fun detailsParams(loadingState: DetailsLoadingState): DetailsScreenParams =
    DetailsScreenParams(
        uiState = DetailsUiState(loadingState = loadingState),
        onNavigateBack = {},
        onViewOnMap = {},
        onToggleFavorite = {},
        onDelete = {}
    )

private fun previewProfile(): List<ElevationSample> = listOf(
    ElevationSample(0.0, 980.0),
    ElevationSample(3_000.0, 1_400.0),
    ElevationSample(6_200.0, 2_100.0),
    ElevationSample(9_000.0, 2_600.0),
    ElevationSample(12_400.0, 2_864.0)
)
