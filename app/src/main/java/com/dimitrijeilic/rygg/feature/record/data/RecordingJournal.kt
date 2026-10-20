package com.dimitrijeilic.rygg.feature.record.data

import android.os.SystemClock
import com.dimitrijeilic.rygg.core.common.outcomeCatching
import com.dimitrijeilic.rygg.core.gpx.model.GpxPoint
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingDao
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingPointEntity
import com.dimitrijeilic.rygg.feature.record.data.local.RecordingSessionEntity
import com.dimitrijeilic.rygg.feature.record.domain.RecordingState
import com.dimitrijeilic.rygg.feature.record.domain.RecoveredRecording
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

// Write-behind log of the live recording, so a process kill costs seconds of track, not the hike.
// Fixes are batched to avoid a disk write per GPS fix; every write runs in order on one queue.
@Singleton
class RecordingJournal @Inject constructor(
    private val dao: RecordingDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val writes = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    private val buffer = mutableListOf<RecordingPointEntity>()
    private var header: RecordingSessionEntity? = null
    private var lastFlushAt = 0L

    init {
        scope.launch {
            for (write in writes) outcomeCatching { write() }
        }
    }

    fun begin(discipline: Discipline, startInstant: Instant) {
        buffer.clear()
        lastFlushAt = SystemClock.elapsedRealtime()
        val session = RecordingSessionEntity(
            discipline = discipline.name,
            startedAt = startInstant.toEpochMilli(),
            state = RecordingState.RECORDING.name,
            elapsedMillis = 0L,
            updatedAt = System.currentTimeMillis()
        )
        header = session
        writes.trySend { dao.begin(session) }
    }

    fun adopt(recovered: RecoveredRecording) {
        buffer.clear()
        lastFlushAt = SystemClock.elapsedRealtime()
        header = RecordingSessionEntity(
            discipline = recovered.discipline.name,
            startedAt = recovered.startInstant.toEpochMilli(),
            state = recovered.state.name,
            elapsedMillis = recovered.elapsedMillis,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun append(point: GpxPoint, segment: Int, state: RecordingState, elapsedMillis: Long) {
        if (header == null) return
        buffer += point.toEntity(segment)
        val due = SystemClock.elapsedRealtime() - lastFlushAt >= FLUSH_INTERVAL_MS
        if (buffer.size >= FLUSH_POINT_COUNT || due) checkpoint(state, elapsedMillis)
    }

    fun appendWaypoint(point: GpxPoint, segment: Int, state: RecordingState, elapsedMillis: Long) {
        if (header == null) return
        buffer += point.toEntity(segment, waypointName = point.name.orEmpty())
        checkpoint(state, elapsedMillis)
    }

    fun checkpoint(state: RecordingState, elapsedMillis: Long) {
        val session = header?.copy(
            state = state.name,
            elapsedMillis = elapsedMillis,
            updatedAt = System.currentTimeMillis()
        ) ?: return
        header = session
        val points = buffer.toList()
        buffer.clear()
        lastFlushAt = SystemClock.elapsedRealtime()
        writes.trySend { dao.checkpoint(session, points) }
    }

    fun clear() {
        header = null
        buffer.clear()
        writes.trySend { dao.clear() }
    }

    // Queued behind pending writes so it never reads a half-flushed journal.
    suspend fun load(): RecoveredRecording? {
        val result = CompletableDeferred<RecoveredRecording?>()
        writes.send { result.completeWith(runCatching { read() }) }
        return result.await()
    }

    private suspend fun read(): RecoveredRecording? {
        val session = dao.getSession() ?: return null
        val (waypoints, track) = dao.getPoints().partition { it.waypointName != null }
        return RecoveredRecording(
            discipline = Discipline.entries.firstOrNull { it.name == session.discipline } ?: Discipline.HIKE,
            startInstant = Instant.ofEpochMilli(session.startedAt),
            state = RecordingState.entries.firstOrNull { it.name == session.state } ?: RecordingState.PAUSED,
            elapsedMillis = session.elapsedMillis,
            segments = track.groupBy { it.segment }.values.map { segment -> segment.map { it.toPoint() } },
            waypoints = waypoints.map { it.toPoint() }
        )
    }

    private companion object {
        const val FLUSH_POINT_COUNT = 10
        const val FLUSH_INTERVAL_MS = 15_000L
    }
}

private fun GpxPoint.toEntity(segment: Int, waypointName: String? = null) = RecordingPointEntity(
    segment = segment,
    lat = lat,
    lon = lon,
    ele = ele,
    timeMillis = time?.toEpochMilli(),
    waypointName = waypointName
)

private fun RecordingPointEntity.toPoint() = GpxPoint(
    lat = lat,
    lon = lon,
    ele = ele,
    time = timeMillis?.let(Instant::ofEpochMilli),
    name = waypointName,
    sym = waypointName?.let { WAYPOINT_SYMBOL }
)

internal const val WAYPOINT_SYMBOL = "Flag"
