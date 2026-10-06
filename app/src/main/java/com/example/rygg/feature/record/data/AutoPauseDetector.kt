package com.example.rygg.feature.record.data

import javax.inject.Inject
import javax.inject.Singleton

// Decides when an in-progress recording should auto-pause (the user stopped) and auto-resume
// (moving again), from a stream of per-fix speeds. Pure and stateful only in the debounce timer,
// so it unit-tests without Android. Hysteresis (resume threshold above pause threshold) plus the
// sustained-stop delay keep it from flapping around the walking/standing boundary.
@Singleton
class AutoPauseDetector @Inject constructor() {
    // When the speed first dropped below the pause threshold while recording; null once moving.
    private var belowSince: Long? = null

    fun onFix(speedMps: Double, nowMillis: Long, isAutoPaused: Boolean): AutoPauseAction {
        if (isAutoPaused) {
            return if (speedMps > AUTO_RESUME_SPEED_MPS) {
                belowSince = null
                AutoPauseAction.RESUME
            } else {
                AutoPauseAction.NONE
            }
        }

        if (speedMps >= AUTO_PAUSE_SPEED_MPS) {
            belowSince = null
            return AutoPauseAction.NONE
        }
        val since = belowSince ?: nowMillis.also { belowSince = it }
        return if (nowMillis - since >= AUTO_PAUSE_DELAY_MS) {
            belowSince = null
            AutoPauseAction.PAUSE
        } else {
            AutoPauseAction.NONE
        }
    }

    fun reset() {
        belowSince = null
    }

    private companion object {
        // Discipline-agnostic "complete stop" detection: a stop is ~zero ground speed whatever the
        // activity, so a single low threshold (with hysteresis) beats per-discipline tuning, which
        // would false-pause a genuinely slow stretch (a crawling uphill cyclist, a scrambling hiker).
        const val AUTO_PAUSE_SPEED_MPS = 0.2
        const val AUTO_RESUME_SPEED_MPS = 0.5

        // How long ground speed must stay below the pause threshold before we auto-pause.
        const val AUTO_PAUSE_DELAY_MS = 2_000L
    }
}

enum class AutoPauseAction { NONE, PAUSE, RESUME }
