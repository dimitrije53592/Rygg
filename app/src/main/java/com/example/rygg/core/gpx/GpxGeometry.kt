package com.example.rygg.core.gpx

import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxPoint

fun GpxDocument.trackSegments(): List<List<GpxPoint>> =
    tracks.flatMap { track -> track.segments.map { it.points } } + routes.map { it.points }

fun GpxDocument.trackPaths(): List<List<GeoPoint>> =
    trackSegments().map { segment -> segment.map { GeoPoint(it.lat, it.lon) } }
