package com.example.rygg.feature.record.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.rygg.R
import com.example.rygg.core.ui.components.KeepScreenOn
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.RyggTextField
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.core.ui.utils.formatDistanceKm
import com.example.rygg.core.ui.utils.formatElevationMeters
import com.example.rygg.core.ui.utils.formatSpeedKmh
import com.example.rygg.core.ui.utils.formatStopwatch
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.record.domain.RecordingState
import com.example.rygg.feature.record.ui.viewmodel.RecordUiState

@Composable
fun RecordScreen(params: RecordScreenParams) {
    val context = LocalContext.current
    var pendingStart by remember { mutableStateOf<Discipline?>(null) }
    var showWaypointDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val discipline = pendingStart
        pendingStart = null
        if (granted && discipline != null) params.onStart(discipline)
    }

    fun requestStart(discipline: Discipline) {
        // Also gate on POST_NOTIFICATIONS: without it the foreground-service notification is
        // silently suppressed on Android 13+, so the recording would run with no status-bar entry.
        if (context.hasLocationPermission() && context.hasNotificationPermission()) {
            params.onStart(discipline)
        } else {
            pendingStart = discipline
            permissionLauncher.launch(recordPermissions())
        }
    }

    if (params.uiState.state != RecordingState.IDLE) KeepScreenOn()

    Scaffold(
        topBar = {
            RyggTopAppBar(title = stringResource(R.string.nav_record), actions = {})
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RyggTheme.getColor(RyggColor.SurfaceDim))
                .padding(innerPadding)
        ) {
            when (params.uiState.state) {
                RecordingState.IDLE ->
                    IdleContent(
                        uiState = params.uiState,
                        onSelectDiscipline = params.onSelectDiscipline,
                        onStart = { requestStart(params.uiState.discipline) }
                    )

                else ->
                    ActiveContent(
                        uiState = params.uiState,
                        onPause = params.onPause,
                        onResume = params.onResume,
                        onStop = params.onStop,
                        onAddWaypoint = { showWaypointDialog = true }
                    )
            }
        }
    }

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
    onSelectDiscipline: (Discipline) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.record_choose_activity),
            style = RyggTheme.typography.titleMedium,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)) {
            Discipline.entries.forEach { discipline ->
                DisciplineChip(
                    discipline = discipline,
                    selected = uiState.discipline == discipline,
                    onClick = { onSelectDiscipline(discipline) }
                )
            }
        }
        BigCircleButton(
            containerColor = RyggTheme.getColor(RyggColor.BrandGreen),
            icon = Icons.Default.FiberManualRecord,
            contentDescription = stringResource(R.string.record_start),
            onClick = onStart
        )
        Text(
            text = stringResource(R.string.record_gps_hint),
            style = RyggTheme.typography.bodySmall,
            color = RyggTheme.getColor(RyggColor.TextSecondary)
        )
    }
}

@Composable
private fun ActiveContent(
    uiState: RecordUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onAddWaypoint: () -> Unit
) {
    val isPaused = uiState.state == RecordingState.PAUSED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)
    ) {
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing8))
        Text(
            text = if (isPaused) stringResource(R.string.record_paused) else stringResource(R.string.record_recording),
            style = RyggTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isPaused) {
                RyggTheme.getColor(RyggColor.TextSecondary)
            } else {
                RyggTheme.getColor(RyggColor.BrandGreen)
            }
        )
        Text(
            text = formatStopwatch(uiState.elapsedMillis),
            style = RyggTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )

        MetricGrid(uiState)

        RyggPrimaryButton(
            text = stringResource(R.string.record_add_waypoint),
            onClick = onAddWaypoint,
            backgroundColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
            textColor = RyggTheme.getColor(RyggColor.BrandGreen),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            MediumCircleButton(
                containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
                iconTint = RyggTheme.getColor(RyggColor.BrandGreen),
                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) {
                    stringResource(R.string.record_resume)
                } else {
                    stringResource(R.string.record_pause)
                },
                onClick = if (isPaused) onResume else onPause
            )
            BigCircleButton(
                containerColor = RyggTheme.getColor(RyggColor.Error),
                icon = Icons.Default.Stop,
                contentDescription = stringResource(R.string.record_stop),
                onClick = onStop
            )
        }
    }
}

@Composable
private fun MetricGrid(uiState: RecordUiState) {
    val emptyValue = stringResource(R.string.follow_stat_empty)
    val stats = listOf(
        stringResource(R.string.record_metric_distance) to formatDistanceKm(uiState.distanceMeters),
        stringResource(R.string.record_metric_speed) to formatSpeedKmh(uiState.currentSpeedMps),
        stringResource(R.string.record_metric_ascent) to formatElevationMeters(uiState.ascentMeters),
        stringResource(R.string.record_metric_elevation) to (uiState.elevationMeters?.let { formatElevationMeters(it) } ?: emptyValue),
        stringResource(R.string.record_metric_points) to uiState.pointCount.toString(),
        stringResource(R.string.record_metric_waypoints) to uiState.waypointCount.toString()
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RyggTheme.dimens.radius16))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(RyggTheme.dimens.commonContentPadding8),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        stats.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(RyggTheme.dimens.commonContentPadding8),
                        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
                    ) {
                        Text(
                            text = value,
                            style = RyggTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RyggTheme.getColor(RyggColor.TextPrimary)
                        )
                        Text(
                            text = label.uppercase(),
                            style = RyggTheme.typography.labelSmall,
                            color = RyggTheme.getColor(RyggColor.TextSecondary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisciplineChip(
    discipline: Discipline,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius12))
            .background(
                if (selected) {
                    RyggTheme.getColor(RyggColor.BrandGreen)
                } else {
                    RyggTheme.getColor(RyggColor.SurfaceElevated)
                }
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding16,
                vertical = RyggTheme.dimens.commonContentPadding12
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Icon(
            painter = painterResource(discipline.iconRes),
            contentDescription = null,
            tint = if (selected) {
                RyggTheme.getColor(RyggColor.OnBrand)
            } else {
                RyggTheme.getColor(RyggColor.TextPrimary)
            },
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
        Text(
            text = discipline.name.capitalize(),
            style = RyggTheme.typography.labelLarge,
            color = if (selected) {
                RyggTheme.getColor(RyggColor.OnBrand)
            } else {
                RyggTheme.getColor(RyggColor.TextPrimary)
            }
        )
    }
}

@Composable
private fun BigCircleButton(
    containerColor: Color,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.recordButtonSize)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = RyggTheme.getColor(RyggColor.OnBrand),
            modifier = Modifier.size(RyggTheme.dimens.iconSize48)
        )
    }
}

@Composable
private fun MediumCircleButton(
    containerColor: Color,
    iconTint: Color,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.iconSize80)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(RyggTheme.dimens.iconSize40)
        )
    }
}

@Composable
private fun WaypointNameDialog(
    defaultName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(defaultName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.record_waypoint_title)) },
        text = {
            RyggTextField(
                value = name,
                onValueChange = { name = it },
                placeholderText = stringResource(R.string.record_waypoint_hint)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.ifBlank { defaultName }) }) {
                Text(
                    text = stringResource(R.string.record_waypoint_save),
                    color = RyggTheme.getColor(RyggColor.BrandGreen)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.record_waypoint_cancel),
                    color = RyggTheme.getColor(RyggColor.TextSecondary)
                )
            }
        }
    )
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

// Below API 33 the notification is shown without a runtime grant; from 33+ it must be granted
// or the foreground-service notification never reaches the status bar.
private fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun recordPermissions(): Array<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}.toTypedArray()

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

data class RecordScreenParams(
    val uiState: RecordUiState,
    val onSelectDiscipline: (Discipline) -> Unit,
    val onStart: (Discipline) -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onStop: () -> Unit,
    val onAddWaypoint: (String) -> Unit
)
