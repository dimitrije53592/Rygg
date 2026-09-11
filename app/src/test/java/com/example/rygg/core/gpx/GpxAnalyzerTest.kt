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
        // Moving basis: ~222 m of moving distance over 20 s of moving time -> ~11.1 m/s.
        assertEquals(11.1, analysis.avgSpeedMps!!, 0.3)
        // Windowed peak over the two ~111 m / 10 s moving segments -> ~11.1 m/s.
        assertEquals(11.1, analysis.maxSpeedMps!!, 0.3)
        assertEquals("test", analysis.creator)
    }

    @Test
    fun analyze_maxSpeed_smoothsSingleFixJitterSpike() {
        // A steady ~1 m/s walk (1 m east each second) with one fix jumping ~10 m out and back.
        // The per-pair speed at the spike is ~10 m/s, but averaged over the >=5 s window the peak
        // stays far below it.
        val meters = listOf(0.0, 1.0, 2.0, 3.0, 13.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        val points = meters.mapIndexed { index, east ->
            GpxPoint(lat = 0.0, lon = metersEastToLon(east), time = Instant.ofEpochSecond(index.toLong()))
        }
        val document = GpxDocument(
            tracks = listOf(Track(segments = listOf(TrackSegment(points = points))))
        )

        val analysis = analyzer.analyze(document)

        // Raw per-pair peak would be ~10 m/s; windowing must keep it well under 5 m/s.
        assertTrue("max speed ${analysis.maxSpeedMps} should be smoothed", analysis.maxSpeedMps!! < 5.0)
    }

    @Test
    fun analyze_maxSpeed_breaksWindowAcrossLongPause() {
        // Two 1 m/s legs separated by a 5-minute stop; the window must not bridge the gap into a
        // fake high speed. Each leg alone spans >=5 s at ~1 m/s.
        val leg1 = (0..6).map { GpxPoint(lat = 0.0, lon = metersEastToLon(it.toDouble()), time = Instant.ofEpochSecond(it.toLong())) }
        val leg2 = (0..6).map { GpxPoint(lat = 0.0, lon = metersEastToLon(1000.0 + it), time = Instant.ofEpochSecond(300L + it)) }
        val document = GpxDocument(
            tracks = listOf(Track(segments = listOf(TrackSegment(points = leg1 + leg2))))
        )

        val analysis = analyzer.analyze(document)

        assertEquals(1.0, analysis.maxSpeedMps!!, 0.3)
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
        assertNull(analysis.avgSpeedMps)
        assertNull(analysis.maxSpeedMps)
        assertNull(analysis.minLat)
    }

    private fun point(lat: Double, lon: Double, ele: Double, second: Long): GpxPoint =
        GpxPoint(lat = lat, lon = lon, ele = ele, time = Instant.ofEpochSecond(second))

    // Longitude offset (at the equator) that corresponds to `meters` east, so fixtures can be
    // written in metres of travel rather than raw degrees.
    private fun metersEastToLon(meters: Double): Double =
        Math.toDegrees(meters / EARTH_RADIUS_METERS)
}
