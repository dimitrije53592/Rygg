package com.dimitrijeilic.rygg.feature.details.ui.paramproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.dimitrijeilic.rygg.core.gpx.model.ElevationSample
import com.dimitrijeilic.rygg.feature.details.ui.screen.DetailsMode
import com.dimitrijeilic.rygg.feature.details.ui.screen.DetailsScreenParams
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.DetailsLoadingState
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.DetailsUiState
import com.dimitrijeilic.rygg.feature.library.ui.paramproviders.previewGpxFileEntry

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
        mode = DetailsMode.View(
            onViewOnMap = {},
            onToggleFavorite = {},
            onRename = {},
            onSaveTrim = { _, _, _ -> },
            onDelete = {},
            onShareLink = {},
            onShareFile = {},
            onSaveFile = {}
        )
    )

private fun previewProfile(): List<ElevationSample> = listOf(
    ElevationSample(0.0, 980.0),
    ElevationSample(3_000.0, 1_400.0),
    ElevationSample(6_200.0, 2_100.0),
    ElevationSample(9_000.0, 2_600.0),
    ElevationSample(12_400.0, 2_864.0)
)
