package com.example.rygg.core.gpx

import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxMetadata
import com.example.rygg.core.gpx.model.GpxPoint
import com.example.rygg.core.gpx.model.Track
import com.example.rygg.core.gpx.model.TrackSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GpxAnalyzerTest {
    private val analyzer = GpxAnalyzer()

    @Test
    fun analyze_computesStatsFromTimedTrack() {
        val points = listOf(
            point(lat = 0.0, lon = 0.0, ele = 100.0, second = 0),
            point(lat = 0.0, lon = 0.001, ele = 110.0, second = 10),
            point(lat = 0.0, lon = 0.001, ele = 110.0, second = 40),
            point(lat = 0.0, lon = 0.002, ele = 105.0, second = 50)
        )
        val document = GpxDocument(
            creator = "test",
            metadata = GpxMetadata(name = "Trail"),
            tracks = listOf(Track(segments = listOf(TrackSegment(points = points)))),
            waypoints = listOf(GpxPoint(lat = 0.0, lon = 0.0))
        )

        val analysis = analyzer.analyze(document)

        assertEquals("Trail", analysis.name)
        assertEquals(222.4, analysis.distanceMeters, 1.0)
        assertEquals(10.0, analysis.ascentMeters, 0.001)
        assertEquals(5.0, analysis.descentMeters, 0.001)
        assertEquals(110.0, analysis.elevationMeters!!, 0.001)
        assertEquals(4, analysis.pointCount)
        assertEquals(1, analysis.routeCount)
        assertEquals(1, analysis.waypointCount)
        assertTrue(analysis.hasTime)
        assertEquals(0L, analysis.startTimeMillis)
        assertEquals(50_000L, analysis.totalTimeMillis)
        assertEquals(20_000L, analysis.movingTimeMillis)
        assertEquals("test", analysis.creator)
    }

    @Test
    fun analyze_emptyDocument_returnsNeutralStats() {
        val analysis = analyzer.analyze(GpxDocument())

        assertEquals("", analysis.name)
        assertEquals(0.0, analysis.distanceMeters, 0.001)
        assertEquals(0.0, analysis.ascentMeters, 0.001)
        assertEquals(0, analysis.pointCount)
        assertFalse(analysis.hasTime)
        assertNull(analysis.elevationMeters)
        assertNull(analysis.startTimeMillis)
        assertNull(analysis.totalTimeMillis)
        assertNull(analysis.movingTimeMillis)
        assertNull(analysis.minLat)
    }

    private fun point(lat: Double, lon: Double, ele: Double, second: Long): GpxPoint =
        GpxPoint(lat = lat, lon = lon, ele = ele, time = Instant.ofEpochSecond(second))
}
