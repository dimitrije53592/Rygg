package com.dimitrijeilic.rygg.feature.details.ui.components

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
internal fun DeletePhotoDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    RyggDialog(
        title = stringResource(R.string.photo_delete_title),
        onDismissRequest = onCancel,
        icon = Icons.Outlined.DeleteOutline,
        iconTint = RyggTheme.getColor(RyggColor.Error),
        iconBackgroundColor = RyggTheme.getColor(RyggColor.Error).copy(alpha = ERROR_BADGE_ALPHA),
        message = stringResource(R.string.photo_delete_message)
    ) {
        RyggPrimaryButton(
            text = stringResource(R.string.library_delete_confirm),
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = RyggTheme.getColor(RyggColor.Error)
        )
        RyggPrimaryButton(
            text = stringResource(R.string.library_delete_cancel),
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
private fun DeletePhotoDialogPreview() {
    RyggTheme {
        DeletePhotoDialog(onConfirm = {}, onCancel = {})
    }
}
