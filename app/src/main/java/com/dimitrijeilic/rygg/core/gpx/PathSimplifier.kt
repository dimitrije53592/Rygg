package com.dimitrijeilic.rygg.core.gpx

import com.dimitrijeilic.rygg.core.gpx.model.GeoPoint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

// Douglas-Peucker on a local equirectangular projection: keeps a trail's shape at overview zoom
// with a fraction of the points, so drawing a whole library stays cheap.
fun simplifyPath(points: List<GeoPoint>, toleranceMeters: Double): List<GeoPoint> {
    if (points.size <= 2) return points
    val metersPerDegLat = EARTH_RADIUS_METERS * PI / 180.0
    val metersPerDegLon = metersPerDegLat * cos(points.first().lat * PI / 180.0)
    val xs = DoubleArray(points.size) { points[it].lon * metersPerDegLon }
    val ys = DoubleArray(points.size) { points[it].lat * metersPerDegLat }

    val keep = BooleanArray(points.size)
    keep[0] = true
    keep[points.lastIndex] = true
    val stack = ArrayDeque<Pair<Int, Int>>()
    stack.addLast(0 to points.lastIndex)
    while (stack.isNotEmpty()) {
        val (first, last) = stack.removeLast()
        var farthest = -1
        var farthestDistance = toleranceMeters
        for (i in first + 1 until last) {
            val distance = segmentDistance(xs[i], ys[i], xs[first], ys[first], xs[last], ys[last])
            if (distance > farthestDistance) {
                farthest = i
                farthestDistance = distance
            }
        }
        if (farthest != -1) {
            keep[farthest] = true
            stack.addLast(first to farthest)
            stack.addLast(farthest to last)
        }
    }
    return points.filterIndexed { index, _ -> keep[index] }
}

private fun segmentDistance(px: Double, py: Double, ax: Double, ay: Double, bx: Double, by: Double): Double {
    val dx = bx - ax
    val dy = by - ay
    val lengthSquared = dx * dx + dy * dy
    val t = if (lengthSquared == 0.0) 0.0 else (((px - ax) * dx + (py - ay) * dy) / lengthSquared).coerceIn(0.0, 1.0)
    val cx = ax + t * dx - px
    val cy = ay + t * dy - py
    return sqrt(cx * cx + cy * cy)
}
