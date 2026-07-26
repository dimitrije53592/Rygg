package com.example.rygg.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import org.maplibre.compose.camera.CameraState
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
internal fun MetricScaleBar(
    cameraState: CameraState,
    modifier: Modifier = Modifier
) {
    val metersPerDp = cameraState.metersPerDpAtTarget
    if (metersPerDp.isNaN() || metersPerDp <= 0.0) return

    val distanceMeters = niceRoundDistance(metersPerDp * SCALE_BAR_MAX_WIDTH_DP)
    val barWidth = (distanceMeters / metersPerDp).dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius4))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated).copy(alpha = SCALE_BAR_BACKGROUND_ALPHA))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding8,
                vertical = RyggTheme.dimens.commonContentPadding4
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatScaleDistance(distanceMeters),
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

private fun niceRoundDistance(maxMeters: Double): Double {
    val magnitude = 10.0.pow(floor(log10(maxMeters)))
    val normalized = maxMeters / magnitude
    val nice = when {
        normalized >= 5.0 -> 5.0
        normalized >= 2.0 -> 2.0
        else -> 1.0
    }
    return nice * magnitude
}

private fun formatScaleDistance(meters: Double): String =
    if (meters >= METERS_PER_KM) {
        val km = meters / METERS_PER_KM
        if (km % 1.0 == 0.0) "${km.toInt()} km" else "$km km"
    } else {
        "${meters.toInt()} m"
    }

private const val SCALE_BAR_MAX_WIDTH_DP = 96.0
private const val SCALE_BAR_BACKGROUND_ALPHA = 0.8f
private const val METERS_PER_KM = 1000.0
