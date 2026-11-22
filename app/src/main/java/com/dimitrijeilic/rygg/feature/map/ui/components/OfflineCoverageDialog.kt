package com.dimitrijeilic.rygg.feature.map.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggDialog
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
internal fun OfflineCoverageDialog(
    isOnline: Boolean,
    onDownload: () -> Unit,
    onFollowAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
    RyggDialog(
        title = stringResource(R.string.map_coverage_title),
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.CloudOff,
        iconTint = RyggTheme.getColor(RyggColor.Warning),
        iconBackgroundColor = RyggTheme.getColor(RyggColor.Warning).copy(alpha = WARNING_BADGE_ALPHA),
        message = stringResource(if (isOnline) R.string.map_coverage_message else R.string.map_coverage_message_offline)
    ) {
        if (isOnline) {
            RyggPrimaryButton(
                text = stringResource(R.string.map_coverage_download),
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth()
            )
            RyggSecondaryButton(
                text = stringResource(R.string.map_coverage_follow_anyway),
                onClick = onFollowAnyway,
                textColor = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            RyggPrimaryButton(
                text = stringResource(R.string.map_coverage_follow_anyway),
                onClick = onFollowAnyway,
                modifier = Modifier.fillMaxWidth()
            )
            RyggSecondaryButton(
                text = stringResource(R.string.offline_cancel),
                onClick = onDismiss,
                textColor = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private const val WARNING_BADGE_ALPHA = 0.12f

@Preview
@Composable
private fun OfflineCoverageDialogPreview() {
    RyggTheme {
        OfflineCoverageDialog(isOnline = true, onDownload = {}, onFollowAnyway = {}, onDismiss = {})
    }
}
