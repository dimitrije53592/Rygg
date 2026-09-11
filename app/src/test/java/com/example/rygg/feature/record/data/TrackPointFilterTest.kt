package com.example.rygg.feature.record.data

import com.example.rygg.core.gpx.EARTH_RADIUS_METERS
import com.example.rygg.feature.record.domain.RecordingResolution
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackPointFilterTest {
    private val filter = TrackPointFilter()
    private val resolution = RecordingResolution.BALANCED

    @Test
    fun firstFix_isAlwaysKept() {
        assertTrue(filter.shouldKeep(0.0, 0.0, 0L, resolution))
    }

    @Test
    fun fixBelowDistanceAndIntervalThresholds_isDropped() {
        filter.shouldKeep(0.0, 0.0, 0L, resolution)

        // ~3 m along the same heading, 1 s later: under the 10 m gate and the 20 s gate.
        assertFalse(filter.shouldKeep(0.0, lonForMetersEast(3.0), 1_000L, resolution))
    }

    @Test
    fun crossingDistanceThreshold_isKept() {
        filter.shouldKeep(0.0, 0.0, 0L, resolution)

        assertTrue(filter.shouldKeep(0.0, lonForMetersEast(12.0), 1_000L, resolution))
    }

    @Test
    fun slowCrawl_isKeptByMaxInterval() {
        filter.shouldKeep(0.0, 0.0, 0L, resolution)

        // Barely moved, but past maxIntervalSeconds (20 s) so the track keeps a heartbeat.
        assertTrue(filter.shouldKeep(0.0, lonForMetersEast(1.0), 25_000L, resolution))
    }

    @Test
    fun sharpTurn_isKeptBeforeDistanceThreshold() {
        filter.shouldKeep(0.0, 0.0, 0L, resolution)
        // Establish an eastward heading with a kept point.
        assertTrue(filter.shouldKeep(0.0, lonForMetersEast(12.0), 1_000L, resolution))

        // Now head north only ~5 m: under the distance gate, but a 90 degree corner.
        assertTrue(filter.shouldKeep(latForMetersNorth(5.0), lonForMetersEast(12.0), 2_000L, resolution))
    }

    @Test
    fun jitterUnderMoveMinimum_doesNotCountAsTurn() {
        filter.shouldKeep(0.0, 0.0, 0L, resolution)
        assertTrue(filter.shouldKeep(0.0, lonForMetersEast(12.0), 1_000L, resolution))

        // A ~1 m sideways wobble is below the bearing move minimum, so it must not fake a corner.
        assertFalse(filter.shouldKeep(latForMetersNorth(1.0), lonForMetersEast(12.0), 2_000L, resolution))
    }

    @Test
    fun reset_forcesNextFixToBeKept() {
        filter.shouldKeep(0.0, 0.0, 0L, resolution)
        filter.reset()

        assertTrue(filter.shouldKeep(0.0, lonForMetersEast(1.0), 100L, resolution))
    }

    @Test
    fun compactResolution_dropsWhatBalancedKeeps() {
        val compact = TrackPointFilter()
        compact.shouldKeep(0.0, 0.0, 0L, RecordingResolution.COMPACT)

        // 12 m clears Balanced's 10 m gate but not Compact's 25 m.
        assertFalse(compact.shouldKeep(0.0, lonForMetersEast(12.0), 1_000L, RecordingResolution.COMPACT))
    }

    private fun lonForMetersEast(meters: Double): Double = Math.toDegrees(meters / EARTH_RADIUS_METERS)

    private fun latForMetersNorth(meters: Double): Double = Math.toDegrees(meters / EARTH_RADIUS_METERS)
}
