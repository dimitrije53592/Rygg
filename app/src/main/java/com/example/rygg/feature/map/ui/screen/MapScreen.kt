package com.example.rygg.feature.map.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatElevationDelta
import com.example.rygg.core.ui.utils.formatPointCount
import com.example.rygg.core.ui.utils.rememberLocationState
import com.example.rygg.feature.map.domain.RouteOverlay
import com.example.rygg.feature.map.ui.components.MapToolbar
import com.example.rygg.feature.map.ui.components.MetricScaleBar
import com.example.rygg.feature.map.ui.components.RouteMapCanvas
import com.example.rygg.feature.map.ui.util.toPuckLocation
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

@Composable
fun MapScreen(params: MapScreenParams) {
    val cameraState = rememberCameraState()
    val locationState = rememberLocationState()
    var pendingRecenter by remember { mutableStateOf(false) }
    var pendingRebearing by remember { mutableStateOf(false) }
    var focusedRouteId by remember { mutableStateOf(params.focusEntryId) }
    var trackingRouteId by remember { mutableStateOf(params.focusEntryId) }

    LaunchedEffect(Unit) {
        locationState.request()
        if (params.focusEntryId == null) pendingRecenter = true
    }

    LaunchedEffect(locationState.location, pendingRecenter) {
        val location = locationState.location
        if (pendingRecenter && location != null) {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(location.longitude, location.latitude),
                    zoom = 15.0
                )
            )
            pendingRecenter = false
        }
    }

    LaunchedEffect(pendingRebearing) {
        if (pendingRebearing) {
            cameraState.animateTo(
                cameraState.position.copy(bearing = 0.0)
            )
            pendingRebearing = false
        }
    }

    LaunchedEffect(focusedRouteId, params.routes) {
        val start = params.routes.firstOrNull { it.id == focusedRouteId }?.start ?: return@LaunchedEffect
        cameraState.animateTo(
            CameraPosition(target = Position(start.lon, start.lat), zoom = 14.0)
        )
    }

    val currentLocation = locationState.location
    val puckLocation = remember(currentLocation) { currentLocation?.toPuckLocation() }

    Scaffold(
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.nav_map),
                actions = {}
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RouteMapCanvas(
                styleUrl = params.styleUrl,
                routes = params.routes,
                focusedRouteId = focusedRouteId,
                puckLocation = puckLocation,
                cameraState = cameraState,
                modifier = Modifier.fillMaxSize(),
                onPinClick = { id ->
                    focusedRouteId = id
                    trackingRouteId = id
                }
            )

            MetricScaleBar(
                cameraState = cameraState,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(RyggTheme.dimens.commonContentPadding12)
            )

            MapToolbar(
                isLocationLoading = pendingRecenter,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(RyggTheme.dimens.commonContentPadding16),
                onCompassClick = {
                    pendingRebearing = true
                },
                onRecenterClick = {
                    locationState.request()
                    pendingRecenter = true
                }
            )

            if (locationState.isUnavailable && currentLocation == null) {
                LocationOffBanner(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(RyggTheme.dimens.commonContentPadding12)
                )
            }

            val trackingRoute = params.routes.firstOrNull { it.id == trackingRouteId }
            if (trackingRoute != null) {
                TrackingModeCard(
                    route = trackingRoute,
                    onStart = { params.onStartFollow(trackingRoute.id) },
                    onClose = { trackingRouteId = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(RyggTheme.dimens.commonContentPadding16)
                )
            }
        }
    }
}

@Composable
private fun TrackingModeCard(
    route: RouteOverlay,
    onStart: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onStart,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RyggTheme.dimens.radius16),
        color = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shadowElevation = RyggTheme.dimens.elevation4
    ) {
        Column(
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding20),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.name,
                        style = RyggTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RyggTheme.getColor(RyggColor.TextPrimary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = route.discipline.name.capitalize(),
                        style = RyggTheme.typography.bodySmall,
                        color = RyggTheme.getColor(RyggColor.TextSecondary)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.map_tracking_close),
                    tint = RyggTheme.getColor(RyggColor.TextSecondary),
                    modifier = Modifier
                        .clip(RoundedCornerShape(RyggTheme.dimens.radius12))
                        .clickable(onClick = onClose)
                        .padding(RyggTheme.dimens.commonContentPadding4)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)) {
                routeStats(route).forEach { stat ->
                    Text(
                        text = stat,
                        style = RyggTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = RyggTheme.getColor(RyggColor.TextPrimary)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = RyggTheme.getColor(RyggColor.BrandGreen),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                )
                Text(
                    text = stringResource(R.string.map_start_following),
                    style = RyggTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = RyggTheme.getColor(RyggColor.BrandGreen)
                )
            }
        }
    }
}

private fun routeStats(route: RouteOverlay): List<String> = buildList {
    if (route.distanceMeters > 0.0) add(formatDistanceKm(route.distanceMeters))
    if (route.ascentMeters > 0.0 || route.descentMeters > 0.0) {
        add(formatElevationDelta(route.ascentMeters, route.descentMeters))
    }
    add(formatPointCount(route.pointCount))
}

@Composable
private fun LocationOffBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius12))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding12,
                vertical = RyggTheme.dimens.commonContentPadding8
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.Error),
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
        Text(
            text = stringResource(R.string.map_location_off),
            style = RyggTheme.typography.labelMedium,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}

data class MapScreenParams(
    val styleUrl: String,
    val routes: List<RouteOverlay>,
    val focusEntryId: Long?,
    val onStartFollow: (Long) -> Unit
)
