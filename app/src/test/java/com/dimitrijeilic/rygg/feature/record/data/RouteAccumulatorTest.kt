package com.dimitrijeilic.rygg.feature.record.data

import com.dimitrijeilic.rygg.core.gpx.model.GpxPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RouteAccumulatorTest {
    // ~111.19 m per 0.001° of longitude on the equator.
    private fun segment(vararg lons: Double) = lons.map { GpxPoint(lat = 0.0, lon = it) }

    @Test
    fun restore_rebuildsSegmentsWithoutBridgingTheGap() {
        val route = RouteAccumulator()

        route.restore(
            recordedSegments = listOf(segment(0.000, 0.001), segment(0.010, 0.011)),
            recordedWaypoints = listOf(GpxPoint(lat = 0.0, lon = 0.0005, name = "Hut"))
        )

        val metrics = route.metrics.value
        assertEquals(222.4, metrics.distanceMeters, 1.0)
        assertEquals(4, metrics.pointCount)
        assertEquals(1, metrics.waypointCount)
        val document = route.buildDocument(startInstant = null, creator = "test")!!
        assertEquals(2, document.tracks.single().segments.size)
        assertEquals(1, document.waypoints.size)
    }

    @Test
    fun breakContinuity_onlyOpensSegmentAfterPoints() {
        val route = RouteAccumulator()
        route.restore(listOf(segment(0.000, 0.001)), emptyList())

        route.breakContinuity()
        route.breakContinuity()

        assertEquals(1, route.currentSegment)
        assertEquals(1, route.buildDocument(null, "test")!!.tracks.single().segments.size)
    }

    @Test
    fun buildDocument_needsTwoPoints() {
        val route = RouteAccumulator()
        route.restore(listOf(segment(0.000)), emptyList())

        assertNull(route.buildDocument(null, "test"))
    }
}
