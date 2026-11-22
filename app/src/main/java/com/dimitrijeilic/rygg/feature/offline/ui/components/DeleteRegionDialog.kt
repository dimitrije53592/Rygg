package com.dimitrijeilic.rygg.feature.offline.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggDialog
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
internal fun DeleteRegionDialog(
    regionName: String,
    sizeText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    RyggDialog(
        title = stringResource(R.string.offline_delete_title),
        onDismissRequest = onCancel,
        icon = Icons.Outlined.DeleteOutline,
        iconTint = RyggTheme.getColor(RyggColor.Error),
        iconBackgroundColor = RyggTheme.getColor(RyggColor.Error).copy(alpha = ERROR_BADGE_ALPHA),
        message = stringResource(R.string.offline_delete_message, regionName, sizeText)
    ) {
        RyggPrimaryButton(
            text = stringResource(R.string.offline_delete_confirm),
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = RyggTheme.getColor(RyggColor.Error)
        )
        RyggPrimaryButton(
            text = stringResource(R.string.offline_cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            textColor = RyggTheme.getColor(RyggColor.TextSecondary),
            backgroundColor = RyggTheme.getColor(RyggColor.Surface),
            borderWidth = RyggTheme.dimens.border1,
            borderColor = RyggTheme.getColor(RyggColor.Outline)
        )
    }
}

private const val ERROR_BADGE_ALPHA = 0.12f

@Preview
@Composable
private fun DeleteRegionDialogPreview() {
    RyggTheme {
        DeleteRegionDialog(regionName = "Jungfrau region", sizeText = "38 MB", onConfirm = {}, onCancel = {})
    }
}
