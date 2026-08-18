package com.example.rygg.feature.record.domain

import com.example.rygg.feature.auth.domain.Discipline

// Live view of the in-progress recording, published by RecordingController.
data class RecordingSnapshot(
    val state: RecordingState = RecordingState.IDLE,
    val discipline: Discipline = Discipline.HIKE,
    val elapsedMillis: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedMps: Double = 0.0,
    val ascentMeters: Double = 0.0,
    val elevationMeters: Double? = null,
    val pointCount: Int = 0,
    val waypointCount: Int = 0,
    val gpsReady: Boolean = false
)
