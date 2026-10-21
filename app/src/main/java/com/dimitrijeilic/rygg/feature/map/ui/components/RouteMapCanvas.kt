package com.dimitrijeilic.rygg.feature.map.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dimitrijeilic.rygg.feature.map.domain.RouteOverlay
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.location.BearingWithAccuracy
import org.maplibre.compose.location.Location
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.rememberDefaultOrientationProvider
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.map.RenderOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult

// The waypoint callout is UI-only state, so it lives here and works on every screen that shows a
// map; it closes when the focused route changes, on a tap elsewhere on the map, on a pan or zoom,
// or on back.
@Composable
internal fun RouteMapCanvas(
    styleUrl: String,
    routes: List<RouteOverlay>,
    focusedRouteId: Long?,
    puckLocation: Location?,
    cameraState: CameraState,
    modifier: Modifier = Modifier,
    showPins: Boolean = true,
    onPinClick: (Long) -> Unit = {}
) {
    var selectedWaypoint by remember(focusedRouteId) { mutableStateOf<SelectedWaypoint?>(null) }
    val waypoint = selectedWaypoint?.let { selected ->
        routes.firstOrNull { it.id == selected.routeId }?.waypoints?.getOrNull(selected.index)
    }

    LaunchedEffect(cameraState, focusedRouteId) {
        snapshotFlow { cameraState.isCameraMoving && cameraState.moveReason == CameraMoveReason.GESTURE }
            .collect { isGesture -> if (isGesture) selectedWaypoint = null }
    }

    BackHandler(enabled = waypoint != null) { selectedWaypoint = null }

    Box(modifier = modifier) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri(styleUrl),
            cameraState = cameraState,
            options = MapOptions(
                renderOptions = RenderOptions(renderMode = RenderOptions.RenderMode.TextureView),
                ornamentOptions = OrnamentOptions(
                    isScaleBarEnabled = false,
                    isCompassEnabled = false
                )
            ),
            onMapClick = { _, _ ->
                selectedWaypoint = null
                ClickResult.Pass
            },
            modifier = Modifier.fillMaxSize()
        ) {
            RouteLayers(
                routes = routes,
                focusedRouteId = focusedRouteId,
                showPins = showPins,
                onPinClick = onPinClick,
                selectedWaypoint = selectedWaypoint.takeIf { waypoint != null },
                onWaypointClick = { selectedWaypoint = it }
            )
            if (puckLocation != null) {
                LocationPuck(
                    idPrefix = "user",
                    location = puckLocation,
                    cameraState = cameraState,
                    // Compass heading, so the puck's direction cone points where the phone faces
                    // even while standing still (GPS course only updates while moving).
                    bearing = rememberCompassBearing()
                )
            }
        }
        if (waypoint != null) {
            WaypointCallout(
                waypoint = waypoint,
                cameraState = cameraState,
                userPosition = puckLocation?.position?.value,
                onDismiss = { selectedWaypoint = null }
            )
        }
    }
}

// Device heading from the rotation-vector sensor, or null when the device has no such sensor
// (the library provider throws if the sensor is missing, so guard before subscribing).
@Composable
private fun rememberCompassBearing(): BearingWithAccuracy? {
    val context = LocalContext.current
    val hasRotationSensor = remember {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null
    }
    if (!hasRotationSensor) return null

    val orientation by rememberDefaultOrientationProvider().orientation.collectAsState()
    return orientation?.orientation
}
