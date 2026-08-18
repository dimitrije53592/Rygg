package com.example.rygg.core.common

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

// Reusable stopwatch: accumulates elapsed time across pause/resume and ticks once a second
// while running. Uses the monotonic elapsedRealtime clock, so wall-clock changes don't skew it.
class RyggTimer @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _elapsed = MutableStateFlow(0L)
    val elapsed: StateFlow<Long> = _elapsed.asStateFlow()

    private var baseMillis = 0L
    private var resumeAtMillis = 0L
    private var running = false
    private var tickJob: Job? = null

    fun start() {
        reset()
        resumeAtMillis = SystemClock.elapsedRealtime()
        running = true
        startTicking()
    }

    fun pause() {
        if (!running) return
        baseMillis += SystemClock.elapsedRealtime() - resumeAtMillis
        running = false
        stopTicking()
        _elapsed.value = baseMillis
    }

    fun resume() {
        if (running) return
        resumeAtMillis = SystemClock.elapsedRealtime()
        running = true
        startTicking()
    }

    fun stop() {
        if (running) {
            baseMillis += SystemClock.elapsedRealtime() - resumeAtMillis
            running = false
        }
        stopTicking()
        _elapsed.value = baseMillis
    }

    fun reset() {
        stopTicking()
        baseMillis = 0L
        resumeAtMillis = 0L
        running = false
        _elapsed.value = 0L
    }

    private fun startTicking() {
        stopTicking()
        tickJob = scope.launch {
            while (isActive) {
                _elapsed.value = currentMillis()
                delay(TICK_INTERVAL_MS)
            }
        }
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun currentMillis(): Long =
        baseMillis + if (running) SystemClock.elapsedRealtime() - resumeAtMillis else 0L

    private companion object {
        const val TICK_INTERVAL_MS = 1_000L
    }
}
