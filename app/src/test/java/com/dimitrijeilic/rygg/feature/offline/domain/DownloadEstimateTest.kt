package com.dimitrijeilic.rygg.feature.offline.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadEstimateTest {
    @Test
    fun wholeWorld_hasOneTileAtZoomZeroAndFourAtZoomOne() {
        val world = RegionBounds(south = -85.0, west = -180.0, north = 85.0, east = 180.0)

        assertEquals(1L, DownloadEstimate.tileCount(world, minZoom = 0, maxZoom = 0))
        assertEquals(5L, DownloadEstimate.tileCount(world, minZoom = 0, maxZoom = 1))
    }

    @Test
    fun singlePoint_countsOneTilePerZoom() {
        val point = RegionBounds(south = 46.5, west = 7.9, north = 46.5, east = 7.9)

        assertEquals(15L, DownloadEstimate.tileCount(point, minZoom = 0, maxZoom = 14))
    }

    @Test
    fun valleyScaleArea_fitsTheProviderLimit() {
        val valley = RegionBounds(south = 46.55, west = 7.95, north = 46.73, east = 8.21)

        val estimate = DownloadEstimate.of(valley)

        assertFalse(estimate.isTooLarge)
        assertTrue(estimate.tileCount in 100L..600L)
    }

    @Test
    fun countryScaleArea_isTooLarge() {
        val switzerland = RegionBounds(south = 45.8, west = 5.9, north = 47.8, east = 10.5)

        assertTrue(DownloadEstimate.of(switzerland).isTooLarge)
    }
}
