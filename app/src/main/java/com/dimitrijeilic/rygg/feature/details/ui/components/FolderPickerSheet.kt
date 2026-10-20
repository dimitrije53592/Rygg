package com.dimitrijeilic.rygg.feature.details.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggRadioRow
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggTextField
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

// Single-level folders: picking one (or typing a new name) moves the route; "No folder" unfiles it.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FolderPickerSheet(
    currentFolder: String?,
    folders: List<String>,
    onFolderPicked: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var newFolder by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shape = RyggShapes.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = RyggTheme.dimens.commonContentPadding24,
                    vertical = RyggTheme.dimens.commonContentPadding12
                ),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            Text(
                text = stringResource(R.string.details_folder_title),
                style = RyggTheme.typography.titleLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            RyggRadioRow(
                label = stringResource(R.string.details_folder_none),
                selected = currentFolder == null,
                onSelect = { onFolderPicked(null) }
            )
            folders.forEach { folder ->
                RyggRadioRow(
                    label = folder,
                    selected = currentFolder == folder,
                    onSelect = { onFolderPicked(folder) }
                )
            }
            RyggTextField(
                value = newFolder,
                onValueChange = { newFolder = it },
                placeholderText = stringResource(R.string.details_folder_new_hint),
                modifier = Modifier.fillMaxWidth()
            )
            RyggSecondaryButton(
                text = stringResource(R.string.details_folder_create),
                onClick = { onFolderPicked(newFolder) },
                isEnabled = newFolder.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
