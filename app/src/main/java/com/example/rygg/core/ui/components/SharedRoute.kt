package com.example.rygg.core.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

// Supplied by AppNavigation so the library card and the details hero can hand the same route
// drawing between screens without every wrapper and params class having to carry the scopes.
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

private data class RouteCanvasKey(val entryId: Long)

// The card canvas and the details hero draw the same route at different sizes, so the track flies
// into place instead of the screen cutting. A no-op on screens that were not reached from the list,
// where there is nothing to match against.
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
