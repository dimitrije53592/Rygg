package com.dimitrijeilic.rygg.feature.map.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OffRouteDetectorTest {
    private val threshold = 60.0

    private fun OffRouteDetector.fix(distance: Double, atSeconds: Long, accuracy: Float = 5f) =
        update(distance, accuracy, threshold, atSeconds * 1_000)

    @Test
    fun briefExcursion_neverAlerts() {
        val detector = OffRouteDetector()

        detector.fix(distance = 120.0, atSeconds = 0)
        detector.fix(distance = 120.0, atSeconds = 5)
        detector.fix(distance = 10.0, atSeconds = 6)

        assertFalse(detector.isOffRoute)
        assertEquals(0, detector.episode)
    }

    @Test
    fun sustainedExcursion_turnsOffRouteOnce() {
        val detector = OffRouteDetector()

        for (second in 0L..30L) detector.fix(distance = 120.0, atSeconds = second)

        assertTrue(detector.isOffRoute)
        assertEquals(1, detector.episode)
    }

    @Test
    fun poorAccuracy_widensTheCorridor() {
        val detector = OffRouteDetector()

        for (second in 0L..20L) detector.fix(distance = 80.0, atSeconds = second, accuracy = 30f)

        assertFalse(detector.isOffRoute)
    }

    @Test
    fun rejoin_needsSeveralGoodFixes_andNextExcursionIsNewEpisode() {
        val detector = OffRouteDetector()
        for (second in 0L..10L) detector.fix(distance = 120.0, atSeconds = second)

        detector.fix(distance = 10.0, atSeconds = 11)
        detector.fix(distance = 10.0, atSeconds = 12)
        assertTrue(detector.isOffRoute)

        detector.fix(distance = 10.0, atSeconds = 13)
        assertFalse(detector.isOffRoute)

        for (second in 20L..30L) detector.fix(distance = 120.0, atSeconds = second)
        assertEquals(2, detector.episode)
    }
}
