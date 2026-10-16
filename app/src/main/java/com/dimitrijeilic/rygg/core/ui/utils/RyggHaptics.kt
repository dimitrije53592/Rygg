package com.dimitrijeilic.rygg.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

// Named by meaning rather than by vibration pattern, so call sites stay rare and deliberate:
// state changes the user must feel through a glove or a pocket, never scrolling or ticking stats.
class RyggHaptics(private val haptic: HapticFeedback) {
    fun confirm() = haptic.performHapticFeedback(HapticFeedbackType.Confirm)

    fun reject() = haptic.performHapticFeedback(HapticFeedbackType.Reject)

    fun toggle(on: Boolean) = haptic.performHapticFeedback(
        if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
    )

    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)

    fun longPress() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberRyggHaptics(): RyggHaptics {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { RyggHaptics(haptic) }
}
