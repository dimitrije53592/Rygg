package com.example.rygg.core.units

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalResources
import com.example.rygg.R
import java.util.Locale
import kotlin.math.roundToInt

class UnitFormatter(
    private val resources: Resources,
    val system: MeasurementSystem
) {
    fun distance(meters: Double): String = when (system) {
        MeasurementSystem.METRIC -> resources.getString(R.string.unit_distance_km, decimal(meters / METERS_PER_KM))
        MeasurementSystem.IMPERIAL -> resources.getString(R.string.unit_distance_mi, decimal(meters / METERS_PER_MILE))
    }

    fun elevation(meters: Double): String = when (system) {
        MeasurementSystem.METRIC -> resources.getString(R.string.unit_elevation_m, whole(meters))
        MeasurementSystem.IMPERIAL -> resources.getString(R.string.unit_elevation_ft, whole(meters * FEET_PER_METER))
    }

    fun ascent(meters: Double): String = resources.getString(R.string.unit_ascent, elevation(meters))

    fun speed(metersPerSecond: Double): String = when (system) {
        MeasurementSystem.METRIC -> resources.getString(R.string.unit_speed_kmh, decimal(metersPerSecond * MPS_TO_KMH))
        MeasurementSystem.IMPERIAL -> resources.getString(R.string.unit_speed_mph, decimal(metersPerSecond * MPS_TO_MPH))
    }

    private fun decimal(value: Double): String = String.format(Locale.getDefault(), "%.1f", value)

    private fun whole(value: Double): String = String.format(Locale.getDefault(), "%,d", value.roundToInt())
}

@Composable
fun rememberUnitFormatter(): UnitFormatter {
    val resources = LocalResources.current
    val system = LocalMeasurementSystem.current
    return remember(resources, system) { UnitFormatter(resources, system) }
}

val LocalMeasurementSystem = staticCompositionLocalOf { MeasurementSystem.METRIC }
