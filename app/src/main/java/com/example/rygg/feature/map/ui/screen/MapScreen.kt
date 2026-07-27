package com.example.rygg.feature.map.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.rememberLocationState
import com.example.rygg.feature.map.domain.RouteOverlay
import com.example.rygg.feature.map.ui.components.MetricScaleBar
import com.example.rygg.feature.map.ui.components.RecenterButton
import com.example.rygg.feature.map.ui.components.RouteLayers
import com.example.rygg.feature.map.ui.util.toPuckLocation
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

@Composable
fun MapScreen(params: MapScreenParams) {
    val cameraState = rememberCameraState()
    val locationState = rememberLocationState()
    var pendingRecenter by remember { mutableStateOf(false) }
    var focusedRouteId by remember { mutableStateOf(params.focusEntryId) }
    var trackingRouteId by remember { mutableStateOf<Long?>(null) }

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
            MaplibreMap(
                baseStyle = BaseStyle.Uri(params.styleUrl),
                cameraState = cameraState,
                options = MapOptions(ornamentOptions = OrnamentOptions(isScaleBarEnabled = false)),
                modifier = Modifier.fillMaxSize()
            ) {
                RouteLayers(
                    routes = params.routes,
                    focusedRouteId = focusedRouteId,
                    onPinClick = { id ->
                        focusedRouteId = id
                        trackingRouteId = id
                    }
                )
                if (puckLocation != null) {
                    LocationPuck(
                        idPrefix = "user",
                        location = puckLocation,
                        cameraState = cameraState
                    )
                }
            }

            MetricScaleBar(
                cameraState = cameraState,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(RyggTheme.dimens.commonContentPadding12)
            )

            val trackingRoute = params.routes.firstOrNull { it.id == trackingRouteId }
            if (trackingRoute != null) {
                TrackingModeCard(
                    route = trackingRoute,
                    onClose = { trackingRouteId = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(RyggTheme.dimens.commonContentPadding16)
                )
            }

            RecenterButton(
                loading = pendingRecenter,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(RyggTheme.dimens.commonContentPadding16),
                onClick = {
                    locationState.request()
                    pendingRecenter = true
                }
            )
        }
    }
}

@Composable
private fun TrackingModeCard(
    route: RouteOverlay,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius16))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(
                start = RyggTheme.dimens.commonContentPadding16,
                end = RyggTheme.dimens.commonContentPadding8,
                top = RyggTheme.dimens.commonContentPadding8,
                bottom = RyggTheme.dimens.commonContentPadding8
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.map_tracking_mode),
                style = RyggTheme.typography.labelSmall,
                color = RyggTheme.getColor(RyggColor.BrandGreen)
            )
            Text(
                text = route.name,
                style = RyggTheme.typography.titleMedium,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.map_tracking_close),
                tint = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        }
    }
}

data class MapScreenParams(
    val styleUrl: String,
    val routes: List<RouteOverlay>,
    val focusEntryId: Long?
)
