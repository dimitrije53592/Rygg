package com.dimitrijeilic.rygg.feature.library.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggDialog
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggTextField
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
internal fun RenameFolderDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    RyggDialog(
        title = stringResource(R.string.library_folder_rename),
        onDismissRequest = onCancel,
        icon = Icons.Outlined.Edit
    ) {
        RyggTextField(
            value = name,
            onValueChange = { name = it },
            labelText = stringResource(R.string.library_folder_name),
            modifier = Modifier.fillMaxWidth()
        )
        RyggPrimaryButton(
            text = stringResource(R.string.library_folder_rename_save),
            onClick = { onConfirm(name) },
            isEnabled = name.isNotBlank() && name.trim() != currentName,
            modifier = Modifier.fillMaxWidth()
        )
        RyggSecondaryButton(
            text = stringResource(R.string.library_delete_cancel),
            onClick = onCancel,
            textColor = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun RenameFolderDialogPreview() {
    RyggTheme {
        RenameFolderDialog(currentName = "Julian Alps", onConfirm = {}, onCancel = {})
    }
}
