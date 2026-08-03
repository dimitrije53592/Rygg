package com.example.rygg.feature.map.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Makes a container a hit-testable node so touches (taps and pinches) are captured here
 * instead of falling through to a map drawn underneath. Child buttons still receive their own taps.
 */
@Composable
internal fun Modifier.absorbTouches(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {}
    )
}
