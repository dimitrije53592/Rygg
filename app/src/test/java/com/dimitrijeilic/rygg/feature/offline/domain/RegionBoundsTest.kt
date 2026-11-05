package com.dimitrijeilic.rygg.feature.offline.domain

import com.dimitrijeilic.rygg.core.gpx.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionBoundsTest {
    private val region = RegionBounds(south = 46.0, west = 7.0, north = 47.0, east = 8.0)

    @Test
    fun contains_innerBoundsAndPoints() {
        assertTrue(region.contains(RegionBounds(south = 46.2, west = 7.2, north = 46.8, east = 7.8)))
        assertTrue(region.contains(GeoPoint(lat = 46.5, lon = 7.5)))
    }

    @Test
    fun contains_rejectsBoundsCrossingTheEdge() {
        assertFalse(region.contains(RegionBounds(south = 46.2, west = 7.2, north = 47.1, east = 7.8)))
        assertFalse(region.contains(GeoPoint(lat = 45.9, lon = 7.5)))
    }

    @Test
    fun of_spansAllPoints() {
        val bounds = RegionBounds.of(listOf(GeoPoint(46.1, 7.4), GeoPoint(46.6, 7.1), GeoPoint(46.3, 7.9)))

        assertEquals(RegionBounds(south = 46.1, west = 7.1, north = 46.6, east = 7.9), bounds)
        assertNull(RegionBounds.of(emptyList()))
    }

    @Test
    fun padded_growsEverySide() {
        val padded = region.padded(0.1)

        assertEquals(45.9, padded.south, 1e-9)
        assertEquals(8.1, padded.east, 1e-9)
    }

    @Test
    fun area_matchesAOneDegreeCellAtThisLatitude() {
        assertEquals(8_500.0, region.areaSquareMeters() / 1_000_000, 300.0)
    }
}
