package com.dimitrijeilic.rygg.feature.offline.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.LoadingIndicator
import com.dimitrijeilic.rygg.core.ui.components.RyggCard
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggStateMessage
import com.dimitrijeilic.rygg.core.ui.components.RyggTopAppBar
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import com.dimitrijeilic.rygg.feature.offline.domain.OfflineRegion
import com.dimitrijeilic.rygg.feature.offline.domain.RegionBounds
import com.dimitrijeilic.rygg.feature.offline.domain.RegionStatus
import com.dimitrijeilic.rygg.feature.offline.ui.components.DeleteRegionDialog
import com.dimitrijeilic.rygg.feature.offline.ui.components.OfflineRegionCard
import com.dimitrijeilic.rygg.feature.offline.ui.components.RenameRegionDialog
import com.dimitrijeilic.rygg.feature.offline.ui.viewmodel.OfflineMapsUiState
import com.dimitrijeilic.rygg.feature.offline.ui.viewmodel.RegionsLoadingState

@Composable
fun OfflineMapsScreen(params: OfflineMapsScreenParams) {
    Scaffold(
        containerColor = RyggTheme.getColor(RyggColor.SurfaceDim),
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.offline_maps_title),
                onNavigateBack = params.onNavigateBack
            )
        },
        bottomBar = { DownloadNewAreaBar(onClick = params.onDownloadNewArea) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = params.uiState.loadingState) {
                RegionsLoadingState.Loading ->
                    LoadingIndicator(text = stringResource(R.string.offline_maps_loading))

                is RegionsLoadingState.Error -> RyggStateMessage(
                    icon = Icons.Outlined.ErrorOutline,
                    tint = RyggTheme.getColor(RyggColor.Error),
                    title = stringResource(R.string.offline_maps_error),
                    body = state.message
                )

                is RegionsLoadingState.Loaded -> if (state.regions.isEmpty()) {
                    RyggStateMessage(
                        icon = Icons.Outlined.Download,
                        title = stringResource(R.string.offline_maps_empty_title),
                        body = stringResource(R.string.offline_maps_empty_body)
                    )
                } else {
                    LoadedContent(state = state, params = params)
                }
            }
        }
    }
}

@Composable
private fun LoadedContent(state: RegionsLoadingState.Loaded, params: OfflineMapsScreenParams) {
    var renaming by remember { mutableStateOf<OfflineRegion?>(null) }
    var deleting by remember { mutableStateOf<OfflineRegion?>(null) }
    val units = rememberUnitFormatter()
    val unnamed = stringResource(R.string.offline_unnamed)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        item { StorageCard(totalBytes = state.totalBytes, freeBytes = state.freeBytes) }
        items(state.regions, key = { it.id }) { region ->
            OfflineRegionCard(
                region = region,
                onOpen = { params.onOpenRegion(region.id) },
                onResume = { params.onResume(region.id) },
                onPause = { params.onPause(region.id) },
                onUpdate = { params.onUpdate(region.id) },
                onRename = { renaming = region },
                onDelete = { deleting = region },
                canStartDownload = !state.hasActiveDownload
            )
        }
    }

    renaming?.let { region ->
        RenameRegionDialog(
            currentName = region.name,
            onConfirm = {
                renaming = null
                params.onRename(region.id, it)
            },
            onCancel = { renaming = null }
        )
    }

    deleting?.let { region ->
        DeleteRegionDialog(
            regionName = region.name.ifBlank { unnamed },
            sizeText = units.fileSize(region.sizeBytes),
            onConfirm = {
                deleting = null
                params.onDelete(region.id)
            },
            onCancel = { deleting = null }
        )
    }
}

@Composable
private fun StorageCard(totalBytes: Long, freeBytes: Long) {
    val units = rememberUnitFormatter()
    RyggCard {
        Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing32)) {
            RyggStat(
                quantity = units.fileSizeParts(totalBytes),
                label = stringResource(R.string.offline_maps_used)
            )
            RyggStat(
                quantity = units.fileSizeParts(freeBytes),
                label = stringResource(R.string.offline_maps_free)
            )
        }
    }
}

@Composable
private fun DownloadNewAreaBar(onClick: () -> Unit) {
    Surface(
        color = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shadowElevation = RyggTheme.dimens.elevation4,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(RyggTheme.dimens.commonContentPadding16)
        ) {
            RyggPrimaryButton(
                text = stringResource(R.string.offline_maps_download_new),
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun OfflineMapsScreenPreview() {
    val bounds = RegionBounds(south = 46.55, west = 7.95, north = 46.73, east = 8.21)
    RyggTheme {
        OfflineMapsScreen(
            params = OfflineMapsScreenParams(
                uiState = OfflineMapsUiState(
                    loadingState = RegionsLoadingState.Loaded(
                        regions = listOf(
                            OfflineRegion(
                                id = 1L,
                                name = "Jungfrau region",
                                bounds = bounds,
                                createdAt = 1_762_300_000_000,
                                updatedAt = 1_762_300_000_000,
                                sourceRouteId = null,
                                sizeBytes = 38_000_000,
                                progress = 1f,
                                status = RegionStatus.Complete
                            ),
                            OfflineRegion(
                                id = 2L,
                                name = "Triglav",
                                bounds = bounds,
                                createdAt = 1_762_300_000_000,
                                updatedAt = 1_762_300_000_000,
                                sourceRouteId = 4L,
                                sizeBytes = 12_000_000,
                                progress = 0.42f,
                                status = RegionStatus.Downloading
                            )
                        ),
                        totalBytes = 50_000_000,
                        freeBytes = 18_400_000_000
                    )
                ),
                onNavigateBack = {},
                onDownloadNewArea = {},
                onOpenRegion = {},
                onResume = {},
                onPause = {},
                onUpdate = {},
                onRename = { _, _ -> },
                onDelete = {}
            )
        )
    }
}

data class OfflineMapsScreenParams(
    val uiState: OfflineMapsUiState,
    val onNavigateBack: () -> Unit,
    val onDownloadNewArea: () -> Unit,
    val onOpenRegion: (Long) -> Unit,
    val onResume: (Long) -> Unit,
    val onPause: (Long) -> Unit,
    val onUpdate: (Long) -> Unit,
    val onRename: (Long, String) -> Unit,
    val onDelete: (Long) -> Unit
)
