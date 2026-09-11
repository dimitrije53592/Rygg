package com.example.rygg.feature.record.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AutoPauseDetectorTest {
    private val detector = AutoPauseDetector()

    @Test
    fun pausesAfterSustainedStop() {
        assertEquals(AutoPauseAction.NONE, detector.onFix(0.1, nowMillis = 0, isAutoPaused = false))
        assertEquals(AutoPauseAction.NONE, detector.onFix(0.1, nowMillis = 1_500, isAutoPaused = false))
        // 3 s below the pause threshold, past the 2 s delay -> pause.
        assertEquals(AutoPauseAction.PAUSE, detector.onFix(0.1, nowMillis = 3_000, isAutoPaused = false))
    }

    @Test
    fun briefStopDoesNotPause() {
        detector.onFix(0.1, nowMillis = 0, isAutoPaused = false)
        detector.onFix(0.1, nowMillis = 1_500, isAutoPaused = false)
        // Movement resumes before the delay elapses -> the stop timer resets.
        assertEquals(AutoPauseAction.NONE, detector.onFix(2.0, nowMillis = 2_000, isAutoPaused = false))
        detector.onFix(0.1, nowMillis = 2_500, isAutoPaused = false)
        assertEquals(AutoPauseAction.NONE, detector.onFix(0.1, nowMillis = 4_000, isAutoPaused = false))
    }

    @Test
    fun resumesWhenMovingAgain() {
        assertEquals(AutoPauseAction.RESUME, detector.onFix(1.5, nowMillis = 20_000, isAutoPaused = true))
    }

    @Test
    fun doesNotResumeBetweenHysteresisThresholds() {
        // Above the pause threshold (0.2) but below the resume threshold (0.5): stay paused.
        assertEquals(AutoPauseAction.NONE, detector.onFix(0.4, nowMillis = 20_000, isAutoPaused = true))
    }

    @Test
    fun resetClearsPendingStop() {
        detector.onFix(0.1, nowMillis = 0, isAutoPaused = false)
        detector.reset()
        // Timer restarts from the next below-threshold fix, so 1.5 s later is short of the delay.
        assertEquals(AutoPauseAction.NONE, detector.onFix(0.1, nowMillis = 1_000, isAutoPaused = false))
        assertEquals(AutoPauseAction.NONE, detector.onFix(0.1, nowMillis = 2_500, isAutoPaused = false))
    }
}
