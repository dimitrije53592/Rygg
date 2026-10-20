package com.dimitrijeilic.rygg.core.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import kotlin.math.min

// Smoothing splits each corner's single arc into a centre arc plus two transition curves, giving
// the continuous curvature of a squircle rather than a plain rounded rectangle.
class SmoothCornerShape(
    private val topRadius: Dp,
    private val bottomRadius: Dp = topRadius,
    private val smoothing: Float = CORNER_SMOOTHING
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        // A radius past half the short side has no room for the transition curves.
        val limit = min(size.width, size.height) / 2f
        val top = CornerRounding(radius = min(with(density) { topRadius.toPx() }, limit), smoothing = smoothing)
        val bottom = CornerRounding(radius = min(with(density) { bottomRadius.toPx() }, limit), smoothing = smoothing)
        // Vertices listed explicitly (TL, TR, BR, BL) so per-corner rounding never depends on a
        // library's internal vertex order.
        val polygon = RoundedPolygon(
            vertices = floatArrayOf(
                0f,
                0f,
                size.width,
                0f,
                size.width,
                size.height,
                0f,
                size.height
            ),
            perVertexRounding = listOf(top, top, bottom, bottom),
            centerX = size.width / 2f,
            centerY = size.height / 2f
        )
        return Outline.Generic(polygon.toPath().asComposePath())
    }
}

// Radius for a surface nested inside one of radius [outer] with [gap] of padding around it.
fun concentricTo(outer: Dp, gap: Dp): Shape {
    val inner = outer - gap
    return SmoothCornerShape(if (inner.value > 0f) inner else Dp(0f))
}

object RyggShapes {
    val card: Shape = SmoothCornerShape(Dimensions.radius20)
    val sheet: Shape = SmoothCornerShape(Dimensions.radius28)
    val field: Shape = SmoothCornerShape(Dimensions.radius12)
    val chip: Shape = SmoothCornerShape(Dimensions.radius12)
    val thumbnail: Shape = SmoothCornerShape(Dimensions.radius16)
    val dialog: Shape = SmoothCornerShape(Dimensions.radius24)
    val navIndicator: Shape = SmoothCornerShape(Dimensions.radius16)

    // Docked to the bottom edge, so only the top corners round.
    val sheetTop: Shape = SmoothCornerShape(topRadius = Dimensions.radius28, bottomRadius = Dimensions.radius0)

    val extraSmall: Shape = SmoothCornerShape(Dimensions.radius4)
    val small: Shape = SmoothCornerShape(Dimensions.radius8)
    val medium: Shape = SmoothCornerShape(Dimensions.radius12)
    val large: Shape = SmoothCornerShape(Dimensions.radius16)
    val extraLarge: Shape = SmoothCornerShape(Dimensions.radius24)
}

private const val CORNER_SMOOTHING = 0.6f
