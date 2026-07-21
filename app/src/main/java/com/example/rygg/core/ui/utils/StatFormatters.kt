package com.example.rygg.core.ui.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd. MMMM", Locale.getDefault())

fun formatDistanceKm(meters: Double): String =
    String.format(Locale.getDefault(), "%.1f km", meters / 1000.0)

fun formatElevationDelta(ascentMeters: Double, descentMeters: Double): String {
    val net = (ascentMeters - descentMeters).roundToInt()
    val arrow = if (net >= 0) "↑" else "↓"
    return String.format(Locale.getDefault(), "%s %,d m", arrow, abs(net))
}

fun formatPointCount(count: Int): String =
    String.format(Locale.getDefault(), "%,d pts", count)

fun formatDuration(millis: Long): String {
    val totalMinutes = millis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return String.format(Locale.getDefault(), "%d:%02d", hours, minutes)
}

fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dateFormatter)
