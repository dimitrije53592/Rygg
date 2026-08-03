package com.example.rygg.feature.map.ui.util

import com.example.rygg.feature.map.domain.RouteOverlay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

internal const val PROP_ID = "id"
internal const val PROP_NAME = "name"

internal fun routeLineFeatures(routes: List<RouteOverlay>): FeatureCollection<*, *> {
    val features = routes.flatMap { route ->
        route.paths
            .filter { it.size >= 2 }
            .map { path ->
                Feature(
                    geometry = LineString(path.map { Position(it.lon, it.lat) }),
                    properties = buildJsonObject { put(PROP_ID, route.id.toString()) }
                )
            }
    }
    return FeatureCollection(features)
}

internal fun routeStartFeatures(routes: List<RouteOverlay>): FeatureCollection<*, *> {
    val features = routes.mapNotNull { route ->
        val start = route.start ?: return@mapNotNull null
        Feature(
            geometry = Point(Position(start.lon, start.lat)),
            properties = buildJsonObject {
                put(PROP_ID, route.id.toString())
                put(PROP_NAME, route.name)
            }
        )
    }
    return FeatureCollection(features)
}
