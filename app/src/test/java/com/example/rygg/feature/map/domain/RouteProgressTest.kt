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
    fun progressAtVertex_reportsRemainingAndFraction() {
        val progress = geometry.progressFor(lat = 0.0, lon = 0.002)

        assertEquals(2, progress.nearestIndex)
        assertEquals(0.0, progress.distanceToRouteMeters, 1.0)
        assertEquals(111.2, progress.distanceRemainingMeters, 1.0)
        assertEquals(0.667, progress.fractionComplete, 0.01)
        assertTrue(progress.isOnRoute(accuracyMeters = 5f))
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
