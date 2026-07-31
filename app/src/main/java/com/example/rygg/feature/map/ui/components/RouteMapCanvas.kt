package com.example.rygg.feature.map.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.rygg.feature.map.domain.RouteOverlay
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.location.Location
import org.maplibre.compose.location.LocationPuck
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
                cameraState = cameraState
            )
        }
    }
}
