package com.example.rygg.feature.map.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.rygg.feature.map.domain.RouteOverlay
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
        modifier = modifier
    ) {
        RouteLayers(
            routes = routes,
            focusedRouteId = focusedRouteId,
            showPins = showPins,
            onPinClick = onPinClick
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
