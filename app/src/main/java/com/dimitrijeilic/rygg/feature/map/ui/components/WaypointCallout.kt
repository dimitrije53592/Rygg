package com.dimitrijeilic.rygg.feature.map.ui.components

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.gpx.haversineMeters
import com.dimitrijeilic.rygg.core.gpx.model.Waypoint
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggStatSize
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggElevation
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.theme.ryggElevation
import com.dimitrijeilic.rygg.core.ui.utils.rememberRyggHaptics
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraState
import org.maplibre.spatialk.geojson.Position
import java.util.Locale

internal data class SelectedWaypoint(
    val routeId: Long,
    val index: Int
)

// Replaces the tapped pin (hidden by the layer) with a card whose tail sits on the waypoint.
// Placement reads the camera position during layout, so the card follows pans and zooms without
// recomposing.
@Composable
internal fun WaypointCallout(
    waypoint: Waypoint,
    cameraState: CameraState,
    userPosition: Position?,
    onDismiss: () -> Unit
) {
    val projection = cameraState.projection ?: return
    val anchor = remember(waypoint) { Position(waypoint.lon, waypoint.lat) }
    val margin = RyggTheme.dimens.commonContentPadding12
    val visibleState = remember(waypoint) { MutableTransitionState(false).apply { targetState = true } }

    Box(
        modifier = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            layout(constraints.maxWidth, constraints.maxHeight) {
                cameraState.position
                val point = projection.screenLocationFromPosition(anchor)
                val marginPx = margin.roundToPx()
                val maxX = (constraints.maxWidth - placeable.width - marginPx).coerceAtLeast(marginPx)
                placeable.place(
                    x = (point.x.roundToPx() - placeable.width / 2).coerceIn(marginPx, maxX),
                    y = point.y.roundToPx() - placeable.height
                )
            }
        }
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = if (RyggTheme.reduceMotion) {
                EnterTransition.None
            } else {
                fadeIn() + scaleIn(transformOrigin = TransformOrigin(0.5f, 1f))
            }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CalloutCard(waypoint = waypoint, userPosition = userPosition, onDismiss = onDismiss)
                CalloutTail()
            }
        }
    }
}

@Composable
private fun CalloutCard(
    waypoint: Waypoint,
    userPosition: Position?,
    onDismiss: () -> Unit
) {
    val units = rememberUnitFormatter()
    val shape = RyggShapes.card
    Column(
        modifier = Modifier
            .widthIn(max = RyggTheme.dimens.calloutMaxWidth)
            .ryggElevation(level = RyggElevation.Floating, shape = shape)
            .clip(shape)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            Text(
                text = waypoint.name.ifBlank { stringResource(R.string.map_waypoint_unnamed) },
                style = RyggTheme.typography.titleMedium,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                modifier = Modifier.weight(1f, fill = false)
            )
            CalloutIconButton(
                icon = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.map_waypoint_close),
                onClick = onDismiss
            )
        }

        waypoint.description?.let { description ->
            Text(
                text = description,
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier
                    .heightIn(max = RyggTheme.dimens.calloutDescriptionMaxHeight)
                    .verticalScroll(rememberScrollState())
            )
        }

        val distance = userPosition?.let {
            haversineMeters(it.latitude, it.longitude, waypoint.lat, waypoint.lon)
        }
        if (waypoint.ele != null || distance != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)) {
                waypoint.ele?.let { elevation ->
                    RyggStat(
                        quantity = units.elevationParts(elevation),
                        label = stringResource(R.string.map_waypoint_elevation),
                        size = RyggStatSize.Small
                    )
                }
                distance?.let { meters ->
                    RyggStat(
                        quantity = units.distanceParts(meters),
                        label = stringResource(R.string.map_waypoint_from_you),
                        size = RyggStatSize.Small
                    )
                }
            }
        }

        CoordinatesRow(waypoint)
    }
}

@Composable
private fun CoordinatesRow(waypoint: Waypoint) {
    val clipboard = LocalClipboard.current
    val haptics = rememberRyggHaptics()
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    val coordinates = remember(waypoint) {
        String.format(Locale.US, COORDINATE_FORMAT, waypoint.lat, waypoint.lon)
    }
    val clipLabel = stringResource(R.string.map_waypoint_coordinates)

    LaunchedEffect(copied) {
        if (copied) {
            delay(COPIED_FEEDBACK_MILLIS)
            copied = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Text(
            text = coordinates,
            style = RyggTheme.typography.bodySmall,
            color = RyggTheme.getColor(RyggColor.TextSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        CalloutIconButton(
            icon = if (copied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
            contentDescription = stringResource(
                if (copied) R.string.map_waypoint_copied else R.string.map_waypoint_copy
            ),
            onClick = {
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(clipLabel, coordinates)))
                    haptics.confirm()
                    copied = true
                }
            }
        )
    }
}

@Composable
private fun CalloutIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.buttonSize32)
            .clip(RyggShapes.small)
            .background(RyggTheme.getColor(RyggColor.Surface))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.size(RyggTheme.dimens.iconSize18)
        )
    }
}

@Composable
private fun CalloutTail() {
    val color = RyggTheme.getColor(RyggColor.SurfaceElevated)
    Canvas(modifier = Modifier.size(RyggTheme.dimens.calloutTailSize)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            close()
        }
        drawPath(path = path, color = color)
    }
}

private const val COORDINATE_FORMAT = "%.5f, %.5f"
private const val COPIED_FEEDBACK_MILLIS = 2_000L
