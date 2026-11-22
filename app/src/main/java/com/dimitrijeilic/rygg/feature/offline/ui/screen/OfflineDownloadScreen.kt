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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.network.NetworkState
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggStatSize
import com.dimitrijeilic.rygg.core.ui.components.RyggTextField
import com.dimitrijeilic.rygg.core.ui.components.RyggTopAppBar
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.utils.rememberLocationGate
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import com.dimitrijeilic.rygg.feature.map.ui.components.RouteMapCanvas
import com.dimitrijeilic.rygg.feature.offline.domain.DownloadEstimate
import com.dimitrijeilic.rygg.feature.offline.domain.RegionBounds
import com.dimitrijeilic.rygg.feature.offline.ui.components.DownloadFrameOverlay
import com.dimitrijeilic.rygg.feature.offline.ui.components.OfflineRegionLayers
import com.dimitrijeilic.rygg.feature.offline.ui.viewmodel.DownloadBlocker
import com.dimitrijeilic.rygg.feature.offline.ui.viewmodel.OfflineDownloadUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

@Composable
fun OfflineDownloadScreen(params: OfflineDownloadScreenParams) {
    Scaffold(
        containerColor = RyggTheme.getColor(RyggColor.SurfaceDim),
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.offline_download_title),
                onNavigateBack = params.onNavigateBack
            )
        },
        bottomBar = { DownloadPanel(params) }
    ) { innerPadding ->
        FramedMap(
            uiState = params.uiState,
            onFrameChanged = params.onFrameChanged,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun FramedMap(
    uiState: OfflineDownloadUiState,
    onFrameChanged: (RegionBounds) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraState = rememberCameraState()
    val locationGate = rememberLocationGate()
    val density = LocalDensity.current
    val inset = RyggTheme.dimens.commonContentPadding24
    var mapSize by remember { mutableStateOf(DpSize.Zero) }
    var isPositioned by remember { mutableStateOf(false) }
    val currentOnFrameChanged by rememberUpdatedState(onFrameChanged)

    val routeBounds = uiState.routeBounds
    LaunchedEffect(routeBounds) {
        routeBounds ?: return@LaunchedEffect
        cameraState.jumpTo(
            boundingBox = routeBounds.toBoundingBox(),
            padding = PaddingValues(inset * 2)
        )
        isPositioned = true
    }

    LaunchedEffect(locationGate.location) {
        val location = locationGate.location ?: return@LaunchedEffect
        if (isPositioned || uiState.route != null) return@LaunchedEffect
        cameraState.position = CameraPosition(
            target = Position(location.longitude, location.latitude),
            zoom = START_ZOOM
        )
        isPositioned = true
    }

    LaunchedEffect(cameraState, mapSize) {
        if (mapSize == DpSize.Zero) return@LaunchedEffect
        snapshotFlow { cameraState.position }.collectLatest {
            delay(FRAME_SETTLE_MILLIS)
            currentOnFrameChanged(cameraState.frameBounds(mapSize, inset))
        }
    }

    Box(
        modifier = modifier.onSizeChanged {
            mapSize = with(density) { DpSize(it.width.toDp(), it.height.toDp()) }
        }
    ) {
        RouteMapCanvas(
            styleUrl = uiState.styleUrl,
            routes = listOfNotNull(uiState.route),
            focusedRouteId = uiState.route?.id,
            puckLocation = null,
            cameraState = cameraState,
            showPins = false,
            modifier = Modifier.fillMaxSize(),
            layers = { OfflineRegionLayers(regions = uiState.existingRegions) }
        )
        DownloadFrameOverlay(
            inset = inset,
            isTooLarge = uiState.blocker == DownloadBlocker.TooLarge
        )
    }
}

@Composable
private fun DownloadPanel(params: OfflineDownloadScreenParams) {
    val uiState = params.uiState
    val units = rememberUnitFormatter()
    val estimate = uiState.estimate

    Surface(
        color = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shadowElevation = RyggTheme.dimens.elevation4,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            RyggTextField(
                value = uiState.name,
                onValueChange = params.onNameChange,
                labelText = stringResource(R.string.offline_download_name),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)) {
                RyggStat(
                    quantity = units.areaParts(estimate?.areaSquareMeters ?: 0.0),
                    label = stringResource(R.string.offline_download_area),
                    size = RyggStatSize.Small
                )
                RyggStat(
                    quantity = units.fileSizeParts(uiState.freeBytes),
                    label = stringResource(R.string.offline_download_free),
                    size = RyggStatSize.Small
                )
            }
            StatusLine(uiState = uiState)
            RyggPrimaryButton(
                text = stringResource(R.string.offline_download_action),
                onClick = params.onDownload,
                isEnabled = estimate != null && uiState.blocker == null,
                isLoading = uiState.isStarting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusLine(uiState: OfflineDownloadUiState) {
    val (text, color) = when (uiState.blocker) {
        DownloadBlocker.ActiveDownload -> stringResource(R.string.offline_download_busy) to RyggColor.Warning
        DownloadBlocker.Offline -> stringResource(R.string.offline_download_offline) to RyggColor.Warning
        DownloadBlocker.TooLarge -> stringResource(R.string.offline_download_too_large) to RyggColor.Error
        DownloadBlocker.NotEnoughStorage -> stringResource(R.string.offline_download_no_storage) to RyggColor.Error
        null -> if (uiState.network.isMetered) {
            stringResource(R.string.offline_download_metered) to RyggColor.Warning
        } else {
            stringResource(R.string.offline_download_hint) to RyggColor.TextSecondary
        }
    }
    Text(
        text = text,
        style = RyggTheme.typography.bodySmall,
        color = RyggTheme.getColor(color)
    )
}

private suspend fun CameraState.frameBounds(mapSize: DpSize, inset: Dp): RegionBounds {
    val projection = awaitProjection()
    val right = mapSize.width - inset
    val bottom = mapSize.height - inset
    val corners = listOf(
        DpOffset(inset, inset),
        DpOffset(right, inset),
        DpOffset(inset, bottom),
        DpOffset(right, bottom)
    ).map { projection.positionFromScreenLocation(it) }
    return RegionBounds(
        south = corners.minOf { it.latitude }.coerceAtLeast(-RegionBounds.MAX_LATITUDE),
        west = corners.minOf { it.longitude }.coerceAtLeast(-180.0),
        north = corners.maxOf { it.latitude }.coerceAtMost(RegionBounds.MAX_LATITUDE),
        east = corners.maxOf { it.longitude }.coerceAtMost(180.0)
    )
}

private fun RegionBounds.toBoundingBox() = BoundingBox(west = west, south = south, east = east, north = north)

private const val START_ZOOM = 11.0
private const val FRAME_SETTLE_MILLIS = 250L

@Preview
@Composable
private fun OfflineDownloadScreenPreview() {
    RyggTheme {
        OfflineDownloadScreen(
            params = OfflineDownloadScreenParams(
                uiState = OfflineDownloadUiState(
                    styleUrl = "",
                    name = "Jungfrau region",
                    estimate = DownloadEstimate(tileCount = 420, areaSquareMeters = 412_000_000.0),
                    freeBytes = 18_400_000_000,
                    network = NetworkState(isOnline = true, isMetered = true)
                ),
                onNavigateBack = {},
                onNameChange = {},
                onFrameChanged = {},
                onDownload = {}
            )
        )
    }
}

data class OfflineDownloadScreenParams(
    val uiState: OfflineDownloadUiState,
    val onNavigateBack: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onFrameChanged: (RegionBounds) -> Unit,
    val onDownload: () -> Unit
)
