package com.dimitrijeilic.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggTextField
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FolderEditorSheet(
    routes: List<GpxFileEntry>,
    onSave: (name: String, entryIds: Set<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(emptySet<Long>()) }

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
                .padding(
                    horizontal = RyggTheme.dimens.commonContentPadding24,
                    vertical = RyggTheme.dimens.commonContentPadding12
                ),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            Text(
                text = stringResource(R.string.library_folder_new),
                style = RyggTheme.typography.titleLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            RyggTextField(
                value = name,
                onValueChange = { name = it },
                labelText = stringResource(R.string.library_folder_name),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.library_folder_pick_routes).uppercase(),
                style = RyggTheme.textStyles.trackedLabel,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = RyggTheme.dimens.folderRouteListMaxHeight)
            ) {
                items(routes, key = { it.id }) { route ->
                    RouteCheckRow(
                        route = route,
                        checked = route.id in selected,
                        onCheckedChange = { checked ->
                            selected = if (checked) selected + route.id else selected - route.id
                        }
                    )
                }
            }
            RyggPrimaryButton(
                text = stringResource(R.string.library_folder_save),
                onClick = { onSave(name, selected) },
                isEnabled = name.isNotBlank() && selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
            if (selected.isEmpty()) {
                Text(
                    text = stringResource(R.string.library_folder_pick_hint),
                    style = RyggTheme.typography.bodySmall,
                    color = RyggTheme.getColor(RyggColor.TextSecondary),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun RouteCheckRow(
    route: GpxFileEntry,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RyggShapes.chip)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = RyggTheme.dimens.commonContentPadding4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = RyggTheme.getColor(RyggColor.BrandGreen),
                uncheckedColor = RyggTheme.getColor(RyggColor.Outline),
                checkmarkColor = RyggTheme.getColor(RyggColor.OnBrand)
            )
        )
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.iconSize32)
                .clip(RyggShapes.chip)
                .background(RyggTheme.getColor(RyggColor.MossSurface)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(route.discipline.iconRes),
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.BrandGreen),
                modifier = Modifier.size(RyggTheme.dimens.iconSize18)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = route.name,
                style = RyggTheme.typography.bodyLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            route.folder?.let { folder ->
                Text(
                    text = folder,
                    style = RyggTheme.typography.bodySmall,
                    color = RyggTheme.getColor(RyggColor.TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
