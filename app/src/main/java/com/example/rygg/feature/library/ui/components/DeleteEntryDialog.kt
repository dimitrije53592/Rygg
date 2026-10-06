package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggDialog
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

/**
 * Guards the swipe-to-delete gesture on the library. Dismissing (back / outside tap) cancels,
 * so only the explicit Delete press removes the route.
 */
@Composable
internal fun DeleteEntryDialog(
    entryName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    RyggDialog(
        title = stringResource(R.string.library_delete_title),
        onDismissRequest = onCancel,
        icon = Icons.Outlined.DeleteOutline,
        iconTint = RyggTheme.getColor(RyggColor.Error),
        iconBackgroundColor = RyggTheme.getColor(RyggColor.Error).copy(alpha = ERROR_BADGE_ALPHA),
        message = stringResource(R.string.library_delete_message, entryName)
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
private fun DeleteEntryDialogPreview() {
    RyggTheme {
        DeleteEntryDialog(
            entryName = "Triglav north face",
            onConfirm = {},
            onCancel = {}
        )
    }
}
