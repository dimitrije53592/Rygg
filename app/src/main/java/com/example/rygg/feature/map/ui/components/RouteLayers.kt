package com.example.rygg.feature.map.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.map.domain.RouteOverlay
import com.example.rygg.feature.map.ui.util.PROP_ID
import com.example.rygg.feature.map.ui.util.PROP_NAME
import com.example.rygg.feature.map.ui.util.routeLineFeatures
import com.example.rygg.feature.map.ui.util.routeStartFeatures
import com.example.rygg.feature.map.ui.util.routeWaypointFeatures
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult

@Composable
internal fun RouteLayers(
    routes: List<RouteOverlay>,
    focusedRouteId: Long?,
    showPins: Boolean = true,
    onPinClick: (Long) -> Unit = {}
) {
    val allLines = remember(routes) { GeoJsonData.Features(routeLineFeatures(routes)) }
    val focusedLines = remember(routes, focusedRouteId) {
        GeoJsonData.Features(routeLineFeatures(routes.filter { it.id == focusedRouteId }))
    }
    val allLinesSource = rememberGeoJsonSource(allLines)
    val focusedLinesSource = rememberGeoJsonSource(focusedLines)

    LineLayer(
        id = "routes-base",
        source = allLinesSource,
        color = const(RyggTheme.getColor(RyggColor.MutedGray)),
        width = const(3.dp)
    )
    LineLayer(
        id = "routes-focused",
        source = focusedLinesSource,
        color = const(RyggTheme.getColor(RyggColor.BrandGreen)),
        width = const(5.dp)
    )

    // Waypoints of the focused route — shown on the map and while following (not gated by showPins).
    val focusedWaypoints = remember(routes, focusedRouteId) {
        GeoJsonData.Features(routeWaypointFeatures(routes.filter { it.id == focusedRouteId }))
    }
    val focusedWaypointsSource = rememberGeoJsonSource(focusedWaypoints)
    SymbolLayer(
        id = "waypoints-focused",
        source = focusedWaypointsSource,
        iconImage = image(painterResource(R.drawable.ic_map_pin_waypoint)),
        iconSize = const(0.6f),
        iconAnchor = const(SymbolAnchor.Bottom),
        iconAllowOverlap = const(true),
        textField = format(span(feature.get(PROP_NAME).asString())),
        textColor = const(RyggTheme.getColor(RyggColor.TextPrimary)),
        textHaloColor = const(RyggTheme.getColor(RyggColor.SurfaceElevated)),
        textHaloWidth = const(2.dp),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = offset(0f.em, 0.5f.em)
    )

    if (!showPins) return

    Discipline.entries.forEach { discipline ->
        val starts = remember(routes, discipline) {
            GeoJsonData.Features(routeStartFeatures(routes.filter { it.discipline == discipline }))
        }
        val startsSource = rememberGeoJsonSource(starts)
        SymbolLayer(
            id = "pins-${discipline.name}",
            source = startsSource,
            iconImage = image(painterResource(discipline.pinRes)),
            iconSize = const(0.7f),
            iconAnchor = const(SymbolAnchor.Bottom),
            iconAllowOverlap = const(true),
            textField = format(span(feature.get(PROP_NAME).asString())),
            textColor = const(RyggTheme.getColor(RyggColor.TextPrimary)),
            textHaloColor = const(RyggTheme.getColor(RyggColor.SurfaceElevated)),
            textHaloWidth = const(2.dp),
            textAnchor = const(SymbolAnchor.Top),
            textOffset = offset(0f.em, 0.6f.em),
            onClick = { features ->
                features.firstOrNull()
                    ?.properties
                    ?.get(PROP_ID)
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.toLongOrNull()
                    ?.let(onPinClick)
                ClickResult.Consume
            }
        )
    }
}

private val Discipline.pinRes: Int
    get() = when (this) {
        Discipline.HIKE -> R.drawable.ic_map_pin_hike
        Discipline.RIDE -> R.drawable.ic_map_pin_bike
        Discipline.SKI -> R.drawable.ic_map_pin_ski
    }
