package com.example.rygg.feature.map.domain

import com.example.rygg.core.gpx.model.GeoPoint
import com.example.rygg.feature.auth.domain.Discipline

data class RouteOverlay(
    val id: Long,
    val name: String,
    val discipline: Discipline,
    val paths: List<List<GeoPoint>>,
    val start: GeoPoint?
)
