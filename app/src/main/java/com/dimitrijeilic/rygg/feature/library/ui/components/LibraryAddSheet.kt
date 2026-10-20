package com.dimitrijeilic.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryAddSheet(
    onImportRoute: () -> Unit,
    onNewFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shape = RyggShapes.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = RyggTheme.dimens.commonContentPadding16)
        ) {
            Text(
                text = stringResource(R.string.library_add_title),
                style = RyggTheme.typography.titleLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                modifier = Modifier.padding(
                    horizontal = RyggTheme.dimens.commonContentPadding24,
                    vertical = RyggTheme.dimens.commonContentPadding12
                )
            )
            AddOptionRow(
                icon = rememberVectorPainter(Icons.Outlined.FileUpload),
                title = stringResource(R.string.library_import),
                description = stringResource(R.string.library_add_route_description),
                onClick = onImportRoute
            )
            AddOptionRow(
                icon = painterResource(R.drawable.ic_folder),
                title = stringResource(R.string.library_folder_new),
                description = stringResource(R.string.library_add_folder_description),
                onClick = onNewFolder
            )
        }
    }
}

@Composable
private fun AddOptionRow(
    icon: Painter,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding24,
                vertical = RyggTheme.dimens.commonContentPadding12
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.iconSize40)
                .clip(RyggShapes.chip)
                .background(RyggTheme.getColor(RyggColor.MossSurface)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.BrandGreen),
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
        Column {
            Text(
                text = title,
                style = RyggTheme.typography.bodyLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            Text(
                text = description,
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        }
    }
}
