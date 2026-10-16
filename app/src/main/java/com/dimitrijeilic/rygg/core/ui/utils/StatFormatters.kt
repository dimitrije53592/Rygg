package com.dimitrijeilic.rygg.core.ui.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd. MMMM", Locale.getDefault())
private val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())

fun formatPointCount(count: Int): String =
    String.format(Locale.getDefault(), "%,d pts", count)

fun formatPercent(fraction: Double): String =
    String.format(Locale.getDefault(), "%d%%", (fraction * 100).roundToInt())

fun formatStopwatch(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

fun formatDurationHoursMinutes(millis: Long): String {
    val totalMinutes = millis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
    } else {
        String.format(Locale.getDefault(), "%dm", minutes)
    }
}

fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dateFormatter)

fun formatMonthYear(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(monthYearFormatter)

// A locale-independent grouping key, so it does not shift with the rendered heading.
fun monthKey(millis: Long): Int {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    return date.year * 100 + date.monthValue
}
