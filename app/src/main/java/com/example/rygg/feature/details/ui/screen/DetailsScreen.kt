package com.example.rygg.feature.details.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.rygg.R
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.core.ui.components.LoadingIndicator
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.details.ui.components.DetailsContentsCard
import com.example.rygg.feature.details.ui.components.DetailsHeroMap
import com.example.rygg.feature.details.ui.components.DetailsMetaCard
import com.example.rygg.feature.details.ui.components.DetailsSectionTitle
import com.example.rygg.feature.details.ui.components.DetailsStatsGrid
import com.example.rygg.feature.details.ui.components.ElevationProfile
import com.example.rygg.feature.details.ui.paramproviders.DetailsParamsProvider
import com.example.rygg.feature.details.ui.viewmodel.DetailsLoadingState
import com.example.rygg.feature.details.ui.viewmodel.DetailsUiState
import com.example.rygg.feature.library.domain.GpxFileEntry

@Composable
fun DetailsScreen(params: DetailsScreenParams) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RyggTheme.getColor(RyggColor.SurfaceDim))
    ) {
        when (val state = params.uiState.loadingState) {
            is DetailsLoadingState.Loading ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator(text = stringResource(R.string.follow_locating))
                }

            is DetailsLoadingState.Error ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.message ?: stringResource(R.string.details_not_found),
                        style = RyggTheme.typography.titleMedium,
                        color = RyggTheme.getColor(RyggColor.TextPrimary),
                        modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16)
                    )
                }

            is DetailsLoadingState.Loaded ->
                LoadedContent(
                    entry = state.entry,
                    elevationProfile = state.elevationProfile,
                    params = params
                )
        }
    }
}

@Composable
private fun LoadedContent(
    entry: GpxFileEntry,
    elevationProfile: List<ElevationSample>,
    params: DetailsScreenParams
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            DetailsHeroMap(
                entry = entry,
                onNavigateBack = params.onNavigateBack,
                onToggleFavorite = params.onToggleFavorite,
                onDelete = params.onDelete
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RyggTheme.dimens.commonContentPadding16),
                verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
            ) {
                DetailsStatsGrid(entry)

                if (elevationProfile.isNotEmpty()) {
                    ElevationProfile(samples = elevationProfile)
                }

                Section(title = stringResource(R.string.details_contents)) {
                    DetailsContentsCard(entry)
                }

                if (entry.description.isNotBlank() || entry.tags.isNotEmpty()) {
                    Section(title = stringResource(R.string.details_details)) {
                        DetailsMetaCard(entry)
                    }
                }
            }
        }

        BottomActionBar(onViewOnMap = { params.onViewOnMap(entry.id) })
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
        DetailsSectionTitle(text = title)
        content()
    }
}

@Composable
private fun BottomActionBar(onViewOnMap: () -> Unit) {
    Surface(
        color = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shadowElevation = RyggTheme.dimens.elevation4,
        modifier = Modifier.fillMaxWidth()
    ) {
        RyggPrimaryButton(
            text = stringResource(R.string.details_view_on_map),
            onClick = onViewOnMap,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(RyggTheme.dimens.commonContentPadding16)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScreenPreview(
    @PreviewParameter(DetailsParamsProvider::class) params: DetailsScreenParams
) {
    RyggTheme {
        DetailsScreen(params = params)
    }
}

data class DetailsScreenParams(
    val uiState: DetailsUiState,
    val onNavigateBack: () -> Unit,
    val onViewOnMap: (Long) -> Unit,
    val onToggleFavorite: () -> Unit,
    val onDelete: () -> Unit
)
