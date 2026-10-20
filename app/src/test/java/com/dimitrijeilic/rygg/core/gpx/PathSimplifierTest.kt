package com.dimitrijeilic.rygg.core.gpx

import com.dimitrijeilic.rygg.core.gpx.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PathSimplifierTest {
    @Test
    fun straightLine_collapsesToEndpoints() {
        val line = (0..100).map { GeoPoint(0.0, it * 0.0001) }

        val simplified = simplifyPath(line, toleranceMeters = 5.0)

        assertEquals(listOf(line.first(), line.last()), simplified)
    }

    @Test
    fun cornerBeyondTolerance_isKept() {
        // An L-shape: ~111 m east, then ~111 m north. The corner must survive.
        val east = (0..10).map { GeoPoint(0.0, it * 0.0001) }
        val north = (1..10).map { GeoPoint(it * 0.0001, 0.001) }

        val simplified = simplifyPath(east + north, toleranceMeters = 5.0)

        assertEquals(3, simplified.size)
        assertEquals(GeoPoint(0.0, 0.001), simplified[1])
    }

    @Test
    fun jitterWithinTolerance_isDropped() {
        // ±1 m zigzag along a straight line is noise at overview zoom.
        val zigzag = (0..50).map { GeoPoint(if (it % 2 == 0) 0.0 else 0.000009, it * 0.0001) }

        val simplified = simplifyPath(zigzag, toleranceMeters = 5.0)

        assertTrue(simplified.size <= 3)
    }

    @Test
    fun geoPaths_roundTrip() {
        val paths = listOf(
            listOf(GeoPoint(46.1, 13.8), GeoPoint(46.2, 13.9)),
            listOf(GeoPoint(46.3, 14.0), GeoPoint(46.4, 14.1))
        )

        assertEquals(paths, decodeGeoPaths(encodeGeoPaths(paths)))
    }
}
