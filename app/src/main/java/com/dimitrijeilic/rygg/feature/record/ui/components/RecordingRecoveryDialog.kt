package com.dimitrijeilic.rygg.feature.record.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggDialog
import com.dimitrijeilic.rygg.core.ui.components.RyggHoldButton
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.utils.formatStopwatch
import com.dimitrijeilic.rygg.core.ui.utils.rememberRyggHaptics
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import com.dimitrijeilic.rygg.feature.auth.domain.Discipline
import com.dimitrijeilic.rygg.feature.record.ui.viewmodel.RecoverySummary

@Composable
fun RecordingRecoveryDialog(
    summary: RecoverySummary,
    onResume: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    val units = rememberUnitFormatter()
    val haptics = rememberRyggHaptics()

    RyggDialog(
        title = stringResource(R.string.record_recovery_title),
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.History,
        message = stringResource(R.string.record_recovery_message, stringResource(summary.discipline.labelRes))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RyggStat(
                quantity = units.distanceParts(summary.distanceMeters),
                label = stringResource(R.string.record_metric_distance),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            RyggStat(
                value = formatStopwatch(summary.elapsedMillis),
                label = stringResource(R.string.record_recovery_time),
                horizontalAlignment = Alignment.CenterHorizontally
            )
        }
        RyggPrimaryButton(
            text = stringResource(R.string.record_recovery_resume),
            onClick = onResume,
            modifier = Modifier.fillMaxWidth()
        )
        RyggSecondaryButton(
            text = stringResource(R.string.record_recovery_save),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )
        RyggHoldButton(
            icon = Icons.Outlined.DeleteOutline,
            contentDescription = stringResource(R.string.record_recovery_discard),
            onHoldComplete = onDiscard,
            onTap = { haptics.reject() },
            size = RyggTheme.dimens.recordSecondaryButtonSize
        )
        Text(
            text = stringResource(R.string.record_recovery_hold_to_discard),
            style = RyggTheme.typography.labelLarge,
            color = RyggTheme.getColor(RyggColor.Error)
        )
    }
}

@Preview
@Composable
private fun RecordingRecoveryDialogPreview() {
    RyggTheme {
        RecordingRecoveryDialog(
            summary = RecoverySummary(
                discipline = Discipline.HIKE,
                elapsedMillis = 11_520_000L,
                distanceMeters = 8_430.0
            ),
            onResume = {},
            onSave = {},
            onDiscard = {},
            onDismiss = {}
        )
    }
}
