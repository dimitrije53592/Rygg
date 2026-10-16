package com.dimitrijeilic.rygg.feature.record.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.location.LocationPurpose
import com.dimitrijeilic.rygg.core.location.LocationReadiness
import com.dimitrijeilic.rygg.core.ui.components.KeepScreenOn
import com.dimitrijeilic.rygg.core.ui.components.LocationGatePrompt
import com.dimitrijeilic.rygg.core.ui.components.RyggDialog
import com.dimitrijeilic.rygg.core.ui.components.RyggHoldButton
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSegmentedControl
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggStatSize
import com.dimitrijeilic.rygg.core.ui.components.RyggTextField
import com.dimitrijeilic.rygg.core.ui.components.RyggTopAppBar
import com.dimitrijeilic.rygg.core.ui.components.pressScale
import com.dimitrijeilic.rygg.core.ui.components.screenContourLines
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggElevation
import com.dimitrijeilic.rygg.core.ui.theme.RyggMotion
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.theme.ryggElevation
import com.dimitrijeilic.rygg.core.ui.utils.formatStopwatch
import com.dimitrijeilic.rygg.core.ui.utils.notificationsEnabled
import com.dimitrijeilic.rygg.core.ui.utils.rememberLocationGate
import com.dimitrijeilic.rygg.core.ui.utils.rememberRyggHaptics
import com.dimitrijeilic.rygg.core.units.Quantity
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import com.dimitrijeilic.rygg.feature.record.domain.RecordingState
import com.dimitrijeilic.rygg.feature.record.ui.components.GpsStatusChip
import com.dimitrijeilic.rygg.feature.record.ui.components.LocationPausedBanner
import com.dimitrijeilic.rygg.feature.record.ui.components.RecordStartButton
import com.dimitrijeilic.rygg.feature.record.ui.viewmodel.RecordUiState
import kotlinx.coroutines.delay

@Composable
fun RecordScreen(params: RecordScreenParams) {
    val context = LocalContext.current
    val locationGate = rememberLocationGate()
    var pendingStart by remember { mutableStateOf<Discipline?>(null) }
    var showWaypointDialog by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        pendingStart?.let(params.onStart)
        pendingStart = null
    }

    fun startWithNotificationCheck(discipline: Discipline) {
        if (context.hasNotificationPermission()) {
            params.onStart(discipline)
        } else {
            pendingStart = discipline
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun requestStart(discipline: Discipline) {
        locationGate.requestAccess(LocationPurpose.RECORD) { startWithNotificationCheck(discipline) }
    }

    val onTurnOnLocation = { locationGate.requestAccess(LocationPurpose.RECORD) }

    if (params.uiState.state != RecordingState.IDLE) KeepScreenOn()

    Scaffold(
        topBar = {
            RyggTopAppBar(title = stringResource(R.string.nav_record))
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RyggTheme.getColor(RyggColor.SurfaceDim))
                .screenContourLines(RyggTheme.getColor(RyggColor.TextSecondary))
                .padding(innerPadding)
        ) {
            when (params.uiState.state) {
                RecordingState.IDLE ->
                    IdleContent(
                        uiState = params.uiState,
                        gpsReady = if (locationGate.readinessFor(LocationPurpose.RECORD) == LocationReadiness.Ready) {
                            locationGate.location != null
                        } else {
                            null
                        },
                        onSelectDiscipline = params.onSelectDiscipline,
                        onStart = { requestStart(params.uiState.discipline) }
                    )

                else ->
                    ActiveContent(
                        uiState = params.uiState,
                        notificationsOff = !context.notificationsEnabled(),
                        onTurnOnLocation = onTurnOnLocation,
                        onPause = params.onPause,
                        onResume = params.onResume,
                        onStop = params.onStop,
                        onAddWaypoint = { showWaypointDialog = true }
                    )
            }
        }
    }

    LocationGatePrompt(gate = locationGate)

    if (showWaypointDialog) {
        WaypointNameDialog(
            defaultName = stringResource(R.string.record_waypoint_default, params.uiState.waypointCount + 1),
            onConfirm = { name ->
                params.onAddWaypoint(name)
                showWaypointDialog = false
            },
            onDismiss = { showWaypointDialog = false }
        )
    }
}

@Composable
private fun IdleContent(
    uiState: RecordUiState,
    gpsReady: Boolean?,
    onSelectDiscipline: (Discipline) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.record_choose_activity),
            style = RyggTheme.typography.titleMedium,
            color = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing16))
        RyggSegmentedControl(
            options = Discipline.entries,
            selected = uiState.discipline,
            onSelect = onSelectDiscipline,
            label = { stringResource(it.labelRes) },
            icon = { it.iconRes }
        )
        Spacer(Modifier.weight(1f))
        RecordStartButton(gpsReady = gpsReady, onClick = onStart)
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing16))
        GpsStatusText(gpsReady = gpsReady)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun GpsStatusText(gpsReady: Boolean?) {
    val color by animateColorAsState(
        targetValue = if (gpsReady == true) {
            RyggTheme.getColor(RyggColor.BrandGreen)
        } else {
            RyggTheme.getColor(RyggColor.TextSecondary)
        },
        animationSpec = RyggMotion.effects(),
        label = "gpsStatusColor"
    )
    Text(
        text = stringResource(
            when (gpsReady) {
                null -> R.string.record_gps_hint
                false -> R.string.record_gps_waiting
                true -> R.string.record_gps_ready
            }
        ),
        style = RyggTheme.typography.labelLarge,
        color = color,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ActiveContent(
    uiState: RecordUiState,
    notificationsOff: Boolean,
    onTurnOnLocation: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onAddWaypoint: () -> Unit
) {
    val haptics = rememberRyggHaptics()
    val units = rememberUnitFormatter()
    val isPaused = uiState.state == RecordingState.PAUSED || uiState.state == RecordingState.AUTO_PAUSED
    val statusText = when (uiState.state) {
        RecordingState.AUTO_PAUSED -> stringResource(R.string.record_auto_paused)
        RecordingState.PAUSED -> stringResource(R.string.record_paused)
        else -> stringResource(R.string.record_recording)
    }
    var showHoldHint by remember { mutableStateOf(false) }
    LaunchedEffect(showHoldHint) {
        if (showHoldHint) {
            delay(HOLD_HINT_MILLIS)
            showHoldHint = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        RecordingStatus(statusText = statusText, isPaused = isPaused)
        when {
            uiState.locationOff -> LocationPausedBanner(onTurnOn = onTurnOnLocation)
            !uiState.gpsReady && uiState.state == RecordingState.RECORDING -> GpsStatusChip(isReady = false)
        }

        Spacer(Modifier.weight(1f))

        // Time and distance lead; everything else is supporting detail.
        Text(
            text = formatStopwatch(uiState.elapsedMillis),
            style = RyggTheme.typography.displayLarge,
            color = if (isPaused) {
                RyggTheme.getColor(RyggColor.TextSecondary)
            } else {
                RyggTheme.getColor(RyggColor.TextPrimary)
            }
        )
        RyggStat(
            quantity = units.distanceParts(uiState.distanceMeters),
            label = stringResource(R.string.record_metric_distance),
            size = RyggStatSize.Large,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        SecondaryMetrics(uiState)
        Text(
            text = stringResource(
                R.string.record_counts,
                "${uiState.pointCount} ${stringResource(R.string.record_metric_points)}",
                "${uiState.waypointCount} ${stringResource(R.string.record_metric_waypoints)}"
            ).uppercase(),
            style = RyggTheme.textStyles.trackedLabel,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
        if (notificationsOff) {
            Text(
                text = stringResource(R.string.record_notifications_off),
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(
            visible = showHoldHint,
            enter = fadeIn(RyggMotion.effects()),
            exit = fadeOut(RyggMotion.effectsFast())
        ) {
            Text(
                text = stringResource(R.string.record_hold_to_stop),
                style = RyggTheme.typography.labelLarge,
                color = RyggTheme.getColor(RyggColor.Error)
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ControlButton(
                icon = Icons.Outlined.AddLocationAlt,
                contentDescription = stringResource(R.string.record_add_waypoint),
                onClick = onAddWaypoint
            )
            RyggHoldButton(
                icon = Icons.Default.Stop,
                contentDescription = stringResource(R.string.record_stop),
                onHoldComplete = onStop,
                onTap = {
                    haptics.reject()
                    showHoldHint = true
                }
            )
            ControlButton(
                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) {
                    stringResource(R.string.record_resume)
                } else {
                    stringResource(R.string.record_pause)
                },
                onClick = {
                    haptics.confirm()
                    if (isPaused) onResume() else onPause()
                }
            )
        }
    }
}

@Composable
private fun SecondaryMetrics(uiState: RecordUiState) {
    val units = rememberUnitFormatter()
    val emptyValue = stringResource(R.string.follow_stat_empty)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .ryggElevation(level = RyggElevation.Card, shape = RyggShapes.card)
            .clip(RyggShapes.card)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding12,
                vertical = RyggTheme.dimens.commonContentPadding16
            ),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        RyggStat(
            quantity = units.speedParts(uiState.currentSpeedMps),
            label = stringResource(R.string.record_metric_speed),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        )
        RyggStat(
            quantity = units.elevationParts(uiState.ascentMeters),
            label = stringResource(R.string.record_metric_ascent),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        )
        RyggStat(
            quantity = uiState.elevationMeters?.let { units.elevationParts(it) } ?: Quantity(emptyValue),
            label = stringResource(R.string.record_metric_elevation),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.recordSecondaryButtonSize)
            .pressScale(interactionSource)
            .ryggElevation(level = RyggElevation.Card, shape = CircleShape)
            .clip(CircleShape)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = RyggTheme.getColor(RyggColor.BrandGreen),
            modifier = Modifier.size(RyggTheme.dimens.iconSize32)
        )
    }
}

@Composable
private fun WaypointNameDialog(
    defaultName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = rememberRyggHaptics()
    var name by remember { mutableStateOf(defaultName) }
    RyggDialog(
        title = stringResource(R.string.record_waypoint_title),
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.AddLocationAlt
    ) {
        RyggTextField(
            value = name,
            onValueChange = { name = it },
            placeholderText = stringResource(R.string.record_waypoint_hint),
            modifier = Modifier.fillMaxWidth()
        )
        RyggPrimaryButton(
            text = stringResource(R.string.record_waypoint_save),
            onClick = {
                haptics.confirm()
                onConfirm(name.ifBlank { defaultName })
            },
            modifier = Modifier.fillMaxWidth()
        )
        RyggSecondaryButton(
            text = stringResource(R.string.record_waypoint_cancel),
            onClick = onDismiss,
            textColor = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecordingStatus(
    statusText: String,
    isPaused: Boolean
) {
    val reduceMotion = RyggTheme.reduceMotion
    val transition = rememberInfiniteTransition(label = "recordingPulse")
    val pulse by transition.animateFloat(
        initialValue = PULSE_MIN_ALPHA,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = PULSE_MILLIS),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recordingPulseAlpha"
    )
    val accent by animateColorAsState(
        targetValue = if (isPaused) {
            RyggTheme.getColor(RyggColor.Warning)
        } else {
            RyggTheme.getColor(RyggColor.AccentBright)
        },
        animationSpec = RyggMotion.effects(),
        label = "recordingAccent"
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = STATUS_PILL_ALPHA))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding12,
                vertical = RyggTheme.dimens.commonContentPadding8
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.statusDotSize8)
                .graphicsLayer { alpha = if (isPaused || reduceMotion) 1f else pulse }
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            text = statusText.uppercase(),
            style = RyggTheme.textStyles.trackedLabel,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}

// Below API 33 the notification is shown without a runtime grant; from 33+ it must be granted
// or the foreground-service notification never reaches the status bar.
private fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

@Preview(showBackground = true)
@Composable
private fun RecordScreenIdlePreview() {
    RyggTheme {
        RecordScreen(
            params = RecordScreenParams(
                uiState = RecordUiState(),
                onSelectDiscipline = {},
                onStart = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onAddWaypoint = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordScreenActivePreview() {
    RyggTheme {
        RecordScreen(
            params = RecordScreenParams(
                uiState = RecordUiState(
                    state = RecordingState.RECORDING,
                    elapsedMillis = 5_025_000L,
                    distanceMeters = 12_400.0,
                    currentSpeedMps = 1.4,
                    ascentMeters = 840.0,
                    elevationMeters = 1_920.0,
                    pointCount = 1_204,
                    waypointCount = 3,
                    gpsReady = true
                ),
                onSelectDiscipline = {},
                onStart = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onAddWaypoint = {}
            )
        )
    }
}

data class RecordScreenParams(
    val uiState: RecordUiState,
    val onSelectDiscipline: (Discipline) -> Unit,
    val onStart: (Discipline) -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onStop: () -> Unit,
    val onAddWaypoint: (String) -> Unit
)

private const val PULSE_MILLIS = 900
private const val PULSE_MIN_ALPHA = 0.35f
private const val STATUS_PILL_ALPHA = 0.16f
private const val HOLD_HINT_MILLIS = 2_500L
