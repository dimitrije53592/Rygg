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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
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
import com.example.rygg.feature.map.domain.TourSample
import com.example.rygg.feature.map.ui.components.FarAwayCard
import com.example.rygg.feature.map.ui.components.FollowingHud
import com.example.rygg.feature.map.ui.components.FollowingStatusBar
import com.example.rygg.feature.map.ui.components.MapToolbar
import com.example.rygg.feature.map.ui.components.RouteMapCanvas
import com.example.rygg.feature.map.ui.util.toPuckLocation
import com.example.rygg.feature.map.ui.viewmodel.PREVIEW_SPEED_METERS_PER_SECOND
import com.example.rygg.feature.map.ui.viewmodel.RouteFollowingPhase
import com.example.rygg.feature.map.ui.viewmodel.RouteFollowingUiState
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
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

            val previewFraction = remember { mutableFloatStateOf(0f) }

            PreviewCameraEffect(
                active = params.uiState.routeFollowingPhase == RouteFollowingPhase.PreviewActive,
                samples = params.uiState.samples,
                totalMeters = geometry?.totalMeters ?: 0.0,
                cameraState = cameraState,
                onFraction = { previewFraction.floatValue = it },
                onFinished = params.onPreviewFinished
            )

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
                        val totalMeters = geometry?.totalMeters ?: 0.0
                        // Quantized so the remaining-distance string reformats a couple of hundred
                        // times across a preview rather than on all 60 frames each second.
                        val distanceRemaining by remember(totalMeters) {
                            derivedStateOf {
                                val steps = (previewFraction.floatValue / DISTANCE_TEXT_STEP).toInt()
                                totalMeters * (1.0 - steps * DISTANCE_TEXT_STEP)
                            }
                        }
                        FollowingHud(
                            fractionComplete = { previewFraction.floatValue },
                            distanceRemainingMeters = distanceRemaining,
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
            imageVector = Icons.Outlined.Close,
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
    val onLocationChange: (Location?, Boolean) -> Unit,
    val setFreeLook: (Boolean) -> Unit,
    val setPendingRebearing: (Boolean) -> Unit,
    val startPreview: () -> Unit,
    val onPreviewFinished: () -> Unit,
    val onExit: () -> Unit
)

// Drives the fly-over itself: one camera write per real frame via withFrameNanos, straight onto
// cameraState. Nothing round-trips through a flow or recomposition, so the cadence is the display's
// rather than whatever the recomposer happened to schedule.
@Composable
private fun PreviewCameraEffect(
    active: Boolean,
    samples: List<TourSample>,
    totalMeters: Double,
    cameraState: CameraState,
    onFraction: (Float) -> Unit,
    onFinished: () -> Unit
) {
    LaunchedEffect(active, samples, totalMeters) {
        if (!active || samples.size < 2 || totalMeters <= 0.0) return@LaunchedEffect

        val start = samples.first()
        onFraction(0f)
        // Awaited, so the intro is never cut off mid-flight by the first frame of the fly-over.
        cameraState.animateTo(
            CameraPosition(
                target = Position(start.lon, start.lat),
                zoom = PREVIEW_ZOOM,
                tilt = PREVIEW_TILT,
                bearing = start.bearing
            ),
            PREVIEW_INTRO_MS.milliseconds
        )

        val durationNanos = totalMeters / PREVIEW_SPEED_METERS_PER_SECOND * NANOS_PER_SECOND
        val lastIndex = samples.lastIndex
        var originNanos = 0L
        var previousNanos = 0L
        var bearing = start.bearing
        var fraction = 0.0

        while (fraction < 1.0) {
            val frameNanos = withFrameNanos { it }
            if (originNanos == 0L) {
                originNanos = frameNanos
                previousNanos = frameNanos
            }
            val frameSeconds = (frameNanos - previousNanos) / NANOS_PER_SECOND
            previousNanos = frameNanos

            fraction = ((frameNanos - originNanos) / durationNanos).coerceIn(0.0, 1.0)

            val exact = fraction * lastIndex
            val lowerIndex = exact.toInt()
            val upperIndex = minOf(lowerIndex + 1, lastIndex)
            val weight = exact - lowerIndex
            val a = samples[lowerIndex]
            val b = samples[upperIndex]

            // Ease onto the target heading on a time constant instead of snapping to it, so what
            // is left of a corner after the look-ahead still arrives gradually.
            val target = lerpAngle(a.bearing, b.bearing, weight)
            val smoothing = (frameSeconds / BEARING_TIME_CONSTANT_SECONDS).coerceIn(0.0, 1.0)
            bearing = lerpAngle(bearing, target, smoothing)

            cameraState.position = CameraPosition(
                target = Position(
                    a.lon + (b.lon - a.lon) * weight,
                    a.lat + (b.lat - a.lat) * weight
                ),
                zoom = PREVIEW_ZOOM,
                tilt = PREVIEW_TILT,
                bearing = bearing
            )
            onFraction(fraction.toFloat())
        }

        onFinished()
    }
}

// Shortest-arc interpolation, so a heading crossing 0/360 eases the short way round.
private fun lerpAngle(from: Double, to: Double, fraction: Double): Double {
    val diff = ((to - from + MODULO_OFFSET_DEGREES) % FULL_CIRCLE_DEGREES) - HALF_CIRCLE_DEGREES
    return (from + diff * fraction + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
}

private const val BEARING_FACING_NORTH = 0.0
private const val PREVIEW_ZOOM = 15.0
private const val PREVIEW_TILT = 45.0
private const val PREVIEW_INTRO_MS = 1200L
private const val NANOS_PER_SECOND = 1_000_000_000.0
private const val BEARING_TIME_CONSTANT_SECONDS = 0.25
private const val DISTANCE_TEXT_STEP = 0.005f
private const val FULL_CIRCLE_DEGREES = 360.0
private const val HALF_CIRCLE_DEGREES = 180.0
private const val MODULO_OFFSET_DEGREES = 540.0
