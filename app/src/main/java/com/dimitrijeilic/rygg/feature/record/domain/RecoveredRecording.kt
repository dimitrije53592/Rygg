package com.dimitrijeilic.rygg.feature.record.domain

import com.dimitrijeilic.rygg.core.gpx.model.GpxPoint
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import java.time.Instant

data class RecoveredRecording(
    val discipline: Discipline,
    val startInstant: Instant,
    val state: RecordingState,
    val elapsedMillis: Long,
    val segments: List<List<GpxPoint>>,
    val waypoints: List<GpxPoint>
)
