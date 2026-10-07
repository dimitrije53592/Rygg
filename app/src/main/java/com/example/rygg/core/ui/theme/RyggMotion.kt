package com.example.rygg.core.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

// These mirror Material 3 Expressive's own motion scheme, read off ExpressiveMotionTokens in
// material3 1.4.0. We restate them rather than calling MaterialTheme.motionScheme so the app is not
// bound to @ExperimentalMaterial3ExpressiveApi; swap to the tokens once that opt-in is gone.
//
// The rule underneath the numbers: spatial springs (position, size, layout) overshoot slightly and
// read as alive. Effects springs (opacity, colour) are critically damped — overshooting an alpha
// looks like a glitch, not a flourish.
object RyggMotion {
    fun <T> spatialFast(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.6f, stiffness = 800f)

    fun <T> spatial(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 380f)

    fun <T> spatialSlow(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 200f)

    fun <T> effectsFast(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 3800f)

    fun <T> effects(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 1600f)

    fun <T> effectsSlow(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 800f)

    // Deliberately bouncier than the spatial default: a press should feel like it springs back.
    fun <T> press(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
}
