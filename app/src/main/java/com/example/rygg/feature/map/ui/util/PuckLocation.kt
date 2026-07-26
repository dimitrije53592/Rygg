package com.example.rygg.feature.map.ui.util

import org.maplibre.compose.location.Location
import org.maplibre.compose.location.PositionWithAccuracy
import org.maplibre.spatialk.geojson.Position
import kotlin.time.TimeSource
import android.location.Location as AndroidLocation

internal fun AndroidLocation.toPuckLocation(): Location =
    Location(
        position = PositionWithAccuracy(
            value = Position(longitude, latitude),
            accuracy = null
        ),
        timestamp = TimeSource.Monotonic.markNow()
    )
