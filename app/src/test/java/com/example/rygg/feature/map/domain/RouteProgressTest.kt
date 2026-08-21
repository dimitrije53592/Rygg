package com.example.rygg.feature.map.domain

import com.example.rygg.core.gpx.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteProgressTest {
    // Four points along the equator, ~111.19 m apart per 0.001° of longitude.
    private val geometry = RouteGeometry.from(
        listOf(
            listOf(
                GeoPoint(0.0, 0.000),
                GeoPoint(0.0, 0.001),
                GeoPoint(0.0, 0.002),
                GeoPoint(0.0, 0.003)
            )
        )
    )

    @Test
    fun totalLength_isSumOfSegments() {
        assertEquals(333.6, geometry.totalMeters, 1.0)
    }

    @Test
    fun pointOnLine_isZeroDistanceAndOnRoute() {
        val progress = geometry.progressFor(lat = 0.0, lon = 0.002)

        assertEquals(0.0, progress.distanceToRouteMeters, 1.0)
        assertTrue(progress.isOnRoute(accuracyMeters = 5f))
    }

    @Test
    fun offsetPerpendicular_measuresToSegmentNotVertex() {
        // ~55.6 m north of the line, midway between the vertices at lon 0.001 and 0.002.
        // Nearest-vertex would report ~78.6 m; projecting onto the segment gives the true ~55.6 m.
        val progress = geometry.progressFor(lat = 0.0005, lon = 0.0015)

        assertEquals(55.6, progress.distanceToRouteMeters, 2.0)
    }

    @Test
    fun offRoutePoint_isNotOnRoute() {
        // ~1.1 km north of the line.
        val progress = geometry.progressFor(lat = 0.01, lon = 0.0015)

        assertTrue(progress.distanceToRouteMeters > ON_ROUTE_THRESHOLD_M)
        assertTrue(!progress.isOnRoute(accuracyMeters = 10f))
    }

    @Test
    fun emptyPaths_returnEmptyProgress() {
        val empty = RouteGeometry.from(emptyList())

        assertEquals(0.0, empty.totalMeters, 0.0)
        assertEquals(RouteProgress.EMPTY, empty.progressFor(0.0, 0.0))
    }
}
