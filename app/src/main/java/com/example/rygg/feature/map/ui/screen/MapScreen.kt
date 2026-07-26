package com.example.rygg.feature.map.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.rememberLocationState
import com.example.rygg.feature.map.ui.components.MetricScaleBar
import com.example.rygg.feature.map.ui.components.RecenterButton
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

    LaunchedEffect(Unit) {
        locationState.request()
        pendingRecenter = true
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

data class MapScreenParams(
    val styleUrl: String
)
