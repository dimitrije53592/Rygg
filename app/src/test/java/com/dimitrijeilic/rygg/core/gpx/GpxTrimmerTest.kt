package com.dimitrijeilic.rygg.core.gpx

import com.dimitrijeilic.rygg.core.gpx.model.Extras
import com.dimitrijeilic.rygg.core.gpx.model.GpxDocument
import com.dimitrijeilic.rygg.core.gpx.model.GpxPoint
import com.dimitrijeilic.rygg.core.gpx.model.RawXml
import com.dimitrijeilic.rygg.core.gpx.model.Track
import com.dimitrijeilic.rygg.core.gpx.model.TrackSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GpxTrimmerTest {
    private val trimmer = GpxTrimmer()

    private val points = (0..10).map { step ->
        GpxPoint(
            lat = 0.0,
            lon = step * 0.001,
            ele = 100.0 + step,
            extras = Extras(extensions = RawXml("<extensions><hr>${120 + step}</hr></extensions>"))
        )
    }
    private val document = GpxDocument(
        tracks = listOf(Track(name = "Ridge", segments = listOf(TrackSegment(points = points)))),
        waypoints = listOf(
            GpxPoint(lat = 0.0, lon = 0.0, name = "Start hut"),
            GpxPoint(lat = 0.0, lon = 0.005, name = "Saddle")
        )
    )

    @Test
    fun trim_keepsOnlyPointsInsideTheRange() {
        val trimmed = trimmer.trim(document, startFraction = 0.25, endFraction = 0.75)

        val kept = trimmed.trackSegments().flatten()
        assertEquals(listOf(0.003, 0.004, 0.005, 0.006, 0.007), kept.map { it.lon })
    }

    @Test
    fun trim_preservesPointExtensionsAndTrackMetadata() {
        val trimmed = trimmer.trim(document, startFraction = 0.0, endFraction = 0.5)

        assertEquals("Ridge", trimmed.tracks.single().name)
        assertEquals(points[2].extras, trimmed.trackSegments().flatten()[2].extras)
    }

    @Test
    fun trim_keepsOnlyWaypointsNearTheKeptStretch() {
        val trimmed = trimmer.trim(document, startFraction = 0.4, endFraction = 1.0)

        assertEquals(listOf("Saddle"), trimmed.waypoints.map { it.name })
    }

    @Test
    fun trim_rejectsARangeThatKeepsTooLittle() {
        assertThrows(IllegalArgumentException::class.java) {
            trimmer.trim(document, startFraction = 0.51, endFraction = 0.52)
        }
    }
}
