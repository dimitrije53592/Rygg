package com.dimitrijeilic.rygg.feature.record.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggDialog
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
internal fun BatteryOptimizationDialog(
    onOpenSettings: () -> Unit,
    onStartAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
    RyggDialog(
        title = stringResource(R.string.record_battery_title),
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.BatteryAlert,
        message = stringResource(R.string.record_battery_message)
    ) {
        RyggPrimaryButton(
            text = stringResource(R.string.record_battery_open_settings),
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        )
        RyggSecondaryButton(
            text = stringResource(R.string.record_battery_skip),
            onClick = onStartAnyway,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

internal fun Context.isIgnoringBatteryOptimizations(): Boolean =
    getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(packageName) == true

// The settings list rather than ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, which Play only allows
// for a narrow set of apps and needs an extra permission.
internal fun Context.openBatteryOptimizationSettings() {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Preview
@Composable
private fun BatteryOptimizationDialogPreview() {
    RyggTheme {
        BatteryOptimizationDialog(onOpenSettings = {}, onStartAnyway = {}, onDismiss = {})
    }
}
