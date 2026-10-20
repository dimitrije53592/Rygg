package com.dimitrijeilic.rygg.feature.map.ui.util

import com.dimitrijeilic.rygg.feature.map.domain.RouteOverlay
import com.dimitrijeilic.rygg.feature.map.ui.components.SelectedWaypoint
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

internal const val PROP_ID = "id"
internal const val PROP_NAME = "name"
internal const val PROP_WAYPOINT_INDEX = "waypoint"

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

// [hidden] is the waypoint currently shown as a callout, which takes the pin's place.
internal fun routeWaypointFeatures(
    routes: List<RouteOverlay>,
    hidden: SelectedWaypoint? = null
): FeatureCollection<*, *> {
    val features = routes.flatMap { route ->
        route.waypoints.mapIndexedNotNull { index, waypoint ->
            if (hidden?.routeId == route.id && hidden.index == index) return@mapIndexedNotNull null
            Feature(
                geometry = Point(Position(waypoint.lon, waypoint.lat)),
                properties = buildJsonObject {
                    put(PROP_ID, route.id.toString())
                    put(PROP_NAME, waypoint.name)
                    put(PROP_WAYPOINT_INDEX, index)
                }
            )
        }
    }
    return FeatureCollection(features)
}
