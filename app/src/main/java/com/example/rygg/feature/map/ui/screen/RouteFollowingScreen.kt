package com.example.rygg.feature.map.ui.screen

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.rygg.R
import com.example.rygg.core.ui.components.KeepScreenOn
import com.example.rygg.core.ui.components.LoadingIndicator
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.formatElevationMeters
import com.example.rygg.core.ui.utils.formatSpeedKmh
import com.example.rygg.core.ui.utils.rememberLocationState
import com.example.rygg.feature.map.ui.components.FarAwayCard
import com.example.rygg.feature.map.ui.components.FollowingHud
import com.example.rygg.feature.map.ui.components.FollowingStatusBar
import com.example.rygg.feature.map.ui.components.MapToolbar
import com.example.rygg.feature.map.ui.components.RouteMapCanvas
import com.example.rygg.feature.map.ui.util.CameraAction
import com.example.rygg.feature.map.ui.util.toPuckLocation
import com.example.rygg.feature.map.ui.viewmodel.RouteFollowingPhase
import com.example.rygg.feature.map.ui.viewmodel.RouteFollowingUiState
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RouteFollowingScreen(
    params: RouteFollowingScreenParams
) {
    val cameraState = rememberCameraState()
    val locationState = rememberLocationState()
    val location = locationState.location
    var initialPositioning by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        locationState.request()
    }

    LaunchedEffect(locationState.location, locationState.isUnavailable) {
        params.onLocationChange(locationState.location, locationState.isUnavailable)

        if (initialPositioning) {
            location?.let {
                initialPositioning = false

                launch {
                    cameraState.animateTo(
                        CameraPosition(
                            target = Position(location.longitude, location.latitude),
                            zoom = PREVIEW_ZOOM,
                            tilt = PREVIEW_TILT
                        )
                    )
                }
            }
        }
    }

    KeepScreenOn()

    when (params.uiState.routeFollowingPhase) {
        RouteFollowingPhase.InitialLoading -> {
            CenteredOverlay {
                LoadingIndicator(stringResource(R.string.follow_locating))
            }
        }

        RouteFollowingPhase.LocationUnavailable -> {
            CenteredOverlay {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
                ) {
                    Text(
                        text = stringResource(R.string.map_location_off),
                        style = RyggTheme.typography.titleMedium,
                        color = RyggTheme.getColor(RyggColor.TextPrimary),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = RyggTheme.dimens.commonContentPadding24)
                    )
                    StopButton(onClick = params.onExit)
                }
            }
        }

        else -> {
            val geometry = params.uiState.geometry

            val puckLocation = remember(location) { location?.toPuckLocation() }
            var lastBearing by remember { mutableDoubleStateOf(0.0) }

            LaunchedEffect(cameraState) {
                snapshotFlow { cameraState.moveReason }.collect { reason ->
                    if (reason == CameraMoveReason.GESTURE) params.setFreeLook(true)
                }
            }

            LaunchedEffect(params.cameraAction) {
                if (params.cameraAction is CameraAction.AnimateTo) {
                    cameraState.animateTo(
                        CameraPosition(
                            target = Position(
                                params.cameraAction.longitude,
                                params.cameraAction.latitude
                            ),
                            zoom = params.cameraAction.zoom,
                            tilt = params.cameraAction.tilt,
                            bearing = params.cameraAction.bearing
                        ),
                        params.cameraAction.duration.milliseconds
                    )
                } else if (params.cameraAction is CameraAction.PositionTo) {
                    cameraState.position = CameraPosition(
                        target = Position(
                            params.cameraAction.longitude,
                            params.cameraAction.latitude
                        ),
                        zoom = params.cameraAction.zoom,
                        tilt = params.cameraAction.tilt,
                        bearing = params.cameraAction.bearing
                    )
                }
            }

            LaunchedEffect(params.uiState.pendingRebearing) {
                if (params.uiState.pendingRebearing) {
                    cameraState.animateTo(
                        cameraState.position.copy(bearing = BEARING_FACING_NORTH)
                    )
                    params.setPendingRebearing(false)
                }
            }

            LaunchedEffect(
                location,
                params.uiState.freeLook,
                params.uiState.routeFollowingPhase
            ) {
                val isFollowing = params.uiState.routeFollowingPhase == RouteFollowingPhase.FollowingActive

                if (isFollowing && !params.uiState.freeLook && location != null) {
                    if (location.hasBearing()) lastBearing = location.bearing.toDouble()

                    cameraState.animateTo(
                        CameraPosition(
                            target = Position(location.longitude, location.latitude),
                            zoom = PREVIEW_ZOOM,
                            tilt = PREVIEW_TILT,
                            bearing = lastBearing
                        )
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                RouteMapCanvas(
                    styleUrl = params.uiState.styleUrl,
                    routes = listOf(params.uiState.route!!),
                    focusedRouteId = params.uiState.route.id,
                    puckLocation = puckLocation,
                    cameraState = cameraState,
                    modifier = Modifier.fillMaxSize(),
                    showPins = false
                )

                StopButton(
                    onClick = params.onExit,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(RyggTheme.dimens.commonContentPadding12)
                )

                when (params.uiState.routeFollowingPhase) {
                    RouteFollowingPhase.UserFarAway -> {
                        FarAwayCard(
                            distanceMeters = params.uiState.progress?.distanceToRouteMeters ?: 0.0,
                            onPreview = { params.startPreview() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(RyggTheme.dimens.commonContentPadding16)
                        )
                    }

                    RouteFollowingPhase.PreviewActive -> {
                        FollowingHud(
                            fractionComplete = params.uiState.previewFraction,
                            distanceRemainingMeters = (geometry?.totalMeters ?: 0.0) * (1.0 - params.uiState.previewFraction),
                            speedText = stringResource(R.string.follow_stat_empty),
                            elevationText = stringResource(R.string.follow_stat_empty),
                            offRoute = false,
                            isPreview = true,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                        )
                    }

                    RouteFollowingPhase.FollowingActive -> {
                        FollowingStatusBar(
                            isOnRoute = params.uiState.isOnRoute,
                            distanceToRouteMeters = params.uiState.progress?.distanceToRouteMeters ?: 0.0,
                            speedText = if (location?.hasSpeed() == true) {
                                formatSpeedKmh(location.speed.toDouble())
                            } else {
                                stringResource(R.string.follow_stat_empty)
                            },
                            elevationText = if (location?.hasAltitude() == true) {
                                formatElevationMeters(location.altitude)
                            } else {
                                stringResource(R.string.follow_stat_empty)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                        )

                        if (params.uiState.freeLook) {
                            MapToolbar(
                                isLocationLoading = false,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(RyggTheme.dimens.commonContentPadding16)
                                    .padding(bottom = RyggTheme.dimens.followRouteToolbarPadding),
                                onCompassClick = { params.setPendingRebearing(true) },
                                onRecenterClick = { params.setFreeLook(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius24))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .clickable { onClick() }
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding16,
                vertical = RyggTheme.dimens.commonContentPadding8
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
        Text(
            text = stringResource(R.string.follow_stop),
            style = RyggTheme.typography.labelLarge,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}

@Composable
private fun CenteredOverlay(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RyggTheme.getColor(RyggColor.SurfaceDim)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

data class RouteFollowingScreenParams(
    val uiState: RouteFollowingUiState,
    val cameraAction: CameraAction?,
    val onLocationChange: (Location?, Boolean) -> Unit,
    val setFreeLook: (Boolean) -> Unit,
    val setPendingRebearing: (Boolean) -> Unit,
    val startPreview: () -> Unit,
    val onExit: () -> Unit
)

private const val BEARING_FACING_NORTH = 0.0
private const val PREVIEW_ZOOM = 15.0
private const val PREVIEW_TILT = 45.0
