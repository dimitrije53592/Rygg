package com.example.rygg.core.gpx.model

import java.time.Instant

data class GpxParseResult(
    val gpxDocument: GpxDocument,
    val warnings: List<String>
)

@JvmInline value class RawXml(val value: String)

data class Extras(
    val unknownChildren: List<RawXml> = emptyList(),
    val extensions: RawXml? = null
)

data class GpxDocument(
    val version: String? = null,
    val creator: String? = null,
    val namespaces:  Map<String, String> = emptyMap(),
    val metadata: GpxMetadata? = null,
    val waypoints: List<GpxPoint> = emptyList(),
    val routes: List<Route> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val extras: Extras = Extras()
)

data class GpxPoint(
    val lat: Double,
    val lon: Double,
    val ele: Double? = null,
    val time: Instant? = null,
    val name: String? = null,
    val desc: String? = null,
    val sym: String? = null,
    val type: String? = null,
    val cmt: String? = null,
    val extras: Extras = Extras()
)

data class GpxMetadata(
    val name: String? = null,
    val desc: String? = null,
    val time: Instant? = null,
    val extras: Extras = Extras()
)

data class TrackSegment(
    val points: List<GpxPoint> = emptyList(),
    val extras: Extras = Extras()
)

data class Track(
    val name: String? = null,
    val desc: String? = null,
    val type: String? = null,
    val number: Int? = null,
    val segments: List<TrackSegment> = emptyList(),
    val extras: Extras = Extras()
)

data class Route(
    val name: String? = null,
    val desc: String? = null,
    val number: Int? = null,
    val points: List<GpxPoint> = emptyList(),
    val extras: Extras = Extras()
)