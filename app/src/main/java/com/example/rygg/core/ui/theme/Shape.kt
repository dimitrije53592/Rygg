package com.example.rygg.core.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.toPath
import kotlin.math.min

// A plain rounded rectangle is one circular arc per corner. Smoothing splits that into a centre arc
// plus two transition curves, giving continuous curvature — the difference between a rounded box
// and a squircle, and most of why iOS surfaces look more expensive than stock Android ones.
private const val CORNER_SMOOTHING = 0.6f

class SmoothCornerShape(
    private val radius: Dp,
    private val smoothing: Float = CORNER_SMOOTHING
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val requested = with(density) { radius.toPx() }
        // A radius past half the short side has no room for the transition curves.
        val clamped = min(requested, min(size.width, size.height) / 2f)
        val polygon = RoundedPolygon.rectangle(
            width = size.width,
            height = size.height,
            rounding = CornerRounding(radius = clamped, smoothing = smoothing),
            centerX = size.width / 2f,
            centerY = size.height / 2f
        )
        return Outline.Generic(polygon.toPath().asComposePath())
    }
}

// Named by role, not by value, so a radius change is one edit rather than a sweep of call sites.
// Nested surfaces follow inner = outer - gap (see concentricTo) or the inner corner reads pinched.
object RyggShapes {
    val card: Shape = SmoothCornerShape(Dimensions.radius20)
    val sheet: Shape = SmoothCornerShape(Dimensions.radius28)
    val field: Shape = SmoothCornerShape(Dimensions.radius12)
    val chip: Shape = SmoothCornerShape(Dimensions.radius12)
    val thumbnail: Shape = SmoothCornerShape(Dimensions.radius16)
}

// Radius for a surface nested inside one of radius [outer] with [gap] of padding around it.
fun concentricTo(outer: Dp, gap: Dp): Shape {
    val inner = outer - gap
    return SmoothCornerShape(if (inner.value > 0f) inner else Dp(0f))
}
