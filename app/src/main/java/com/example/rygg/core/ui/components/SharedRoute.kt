package com.example.rygg.core.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// A no-op on screens not reached from the list, where there is nothing to match against.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedRouteCanvas(entryId: Long): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@sharedRouteCanvas.sharedElement(
            sharedContentState = rememberSharedContentState(key = RouteCanvasKey(entryId)),
            animatedVisibilityScope = animatedScope
        )
    }
}

private data class RouteCanvasKey(val entryId: Long)
