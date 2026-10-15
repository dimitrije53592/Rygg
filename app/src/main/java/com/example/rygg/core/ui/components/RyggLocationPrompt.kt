package com.example.rygg.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.location.LocationPurpose
import com.example.rygg.core.location.LocationReadiness
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.LocationGate
import com.example.rygg.core.ui.utils.LocationPrompt

@Composable
fun LocationGatePrompt(gate: LocationGate) {
    val prompt = gate.prompt ?: return
    RyggLocationPrompt(
        prompt = prompt,
        onConfirm = gate::resolvePrompt,
        onDismiss = gate::dismissPrompt
    )
}

@Composable
fun RyggLocationPrompt(
    prompt: LocationPrompt,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val content = promptContent(prompt) ?: return
    RyggDialog(
        title = stringResource(content.titleRes),
        onDismissRequest = onDismiss,
        icon = content.icon,
        message = stringResource(content.bodyRes)
    ) {
        RyggPrimaryButton(
            text = stringResource(content.actionRes),
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        )
        RyggPrimaryButton(
            text = stringResource(R.string.location_prompt_dismiss),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            textColor = RyggTheme.getColor(RyggColor.TextSecondary),
            backgroundColor = RyggTheme.getColor(RyggColor.Surface),
            borderWidth = RyggTheme.dimens.border1,
            borderColor = RyggTheme.getColor(RyggColor.Outline)
        )
    }
}

private fun promptContent(prompt: LocationPrompt): PromptContent? = when (prompt.readiness) {
    LocationReadiness.PermissionNeeded -> PromptContent(
        icon = Icons.Outlined.MyLocation,
        titleRes = R.string.location_prompt_permission_title,
        bodyRes = when (prompt.purpose) {
            LocationPurpose.SHOW_POSITION -> R.string.location_prompt_permission_position
            LocationPurpose.FOLLOW_ROUTE -> R.string.location_prompt_permission_follow
            LocationPurpose.RECORD -> R.string.location_prompt_permission_record
        },
        actionRes = R.string.location_prompt_permission_action
    )

    LocationReadiness.PermissionBlocked -> PromptContent(
        icon = Icons.Outlined.Settings,
        titleRes = R.string.location_prompt_blocked_title,
        bodyRes = R.string.location_prompt_blocked_body,
        actionRes = R.string.location_prompt_blocked_action
    )

    LocationReadiness.ServicesOff -> PromptContent(
        icon = Icons.Outlined.LocationOff,
        titleRes = R.string.location_prompt_services_title,
        bodyRes = when (prompt.purpose) {
            LocationPurpose.SHOW_POSITION -> R.string.location_prompt_services_position
            LocationPurpose.FOLLOW_ROUTE -> R.string.location_prompt_services_follow
            LocationPurpose.RECORD -> R.string.location_prompt_services_record
        },
        actionRes = R.string.location_prompt_services_action
    )

    LocationReadiness.ApproximateOnly -> PromptContent(
        icon = Icons.Outlined.GpsFixed,
        titleRes = R.string.location_prompt_precise_title,
        bodyRes = if (prompt.purpose == LocationPurpose.RECORD) {
            R.string.location_prompt_precise_record
        } else {
            R.string.location_prompt_precise_follow
        },
        actionRes = R.string.location_prompt_precise_action
    )

    LocationReadiness.Ready -> null
}

@Preview
@Composable
private fun RyggLocationPromptServicesOffPreview() {
    RyggTheme {
        RyggLocationPrompt(
            prompt = LocationPrompt(LocationPurpose.RECORD, LocationReadiness.ServicesOff),
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun RyggLocationPromptBlockedPreview() {
    RyggTheme {
        RyggLocationPrompt(
            prompt = LocationPrompt(LocationPurpose.SHOW_POSITION, LocationReadiness.PermissionBlocked),
            onConfirm = {},
            onDismiss = {}
        )
    }
}

private class PromptContent(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    @StringRes val actionRes: Int
)
