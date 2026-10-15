package com.example.rygg.feature.map.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggShapes
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.units.FEET_PER_METER
import com.example.rygg.core.units.LocalMeasurementSystem
import com.example.rygg.core.units.METERS_PER_KM
import com.example.rygg.core.units.METERS_PER_MILE
import com.example.rygg.core.units.MeasurementSystem
import org.maplibre.compose.camera.CameraState
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
internal fun ScaleBar(
    cameraState: CameraState,
    modifier: Modifier = Modifier
) {
    val metersPerDp = cameraState.metersPerDpAtTarget
    if (metersPerDp.isNaN() || metersPerDp <= 0.0) return

    val system = LocalMeasurementSystem.current
    val scale = niceScale(metersPerDp * SCALE_BAR_MAX_WIDTH_DP, system)
    val barWidth = (scale.meters / metersPerDp).dp

    Column(
        modifier = modifier
            .clip(RyggShapes.extraSmall)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated).copy(alpha = SCALE_BAR_BACKGROUND_ALPHA))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding8,
                vertical = RyggTheme.dimens.commonContentPadding4
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(scale.labelRes, scale.amountText),
            style = RyggTheme.typography.labelSmall,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
        Box(
            modifier = Modifier
                .padding(top = RyggTheme.dimens.commonSpacing4)
                .width(barWidth)
                .height(RyggTheme.dimens.border2)
                .background(RyggTheme.getColor(RyggColor.TextPrimary))
        )
    }
}

private fun niceScale(maxMeters: Double, system: MeasurementSystem): Scale = when (system) {
    MeasurementSystem.METRIC -> if (maxMeters >= METERS_PER_KM) {
        val km = niceRound(maxMeters / METERS_PER_KM)
        Scale(km * METERS_PER_KM, km.toAmountText(), R.string.unit_distance_km)
    } else {
        val meters = niceRound(maxMeters)
        Scale(meters, meters.toAmountText(), R.string.unit_elevation_m)
    }

    MeasurementSystem.IMPERIAL -> if (maxMeters >= METERS_PER_MILE) {
        val miles = niceRound(maxMeters / METERS_PER_MILE)
        Scale(miles * METERS_PER_MILE, miles.toAmountText(), R.string.unit_distance_mi)
    } else {
        val feet = niceRound(maxMeters * FEET_PER_METER)
        Scale(feet / FEET_PER_METER, feet.toAmountText(), R.string.unit_elevation_ft)
    }
}

private fun niceRound(max: Double): Double {
    val magnitude = 10.0.pow(floor(log10(max)))
    val normalized = max / magnitude
    val nice = when {
        normalized >= 5.0 -> 5.0
        normalized >= 2.0 -> 2.0
        else -> 1.0
    }
    return nice * magnitude
}

private fun Double.toAmountText(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()

private const val SCALE_BAR_MAX_WIDTH_DP = 96.0
private const val SCALE_BAR_BACKGROUND_ALPHA = 0.8f

private class Scale(val meters: Double, val amountText: String, @StringRes val labelRes: Int)
