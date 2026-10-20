package com.dimitrijeilic.rygg.feature.details.ui.components

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
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsShareSheet(
    isSharingLink: Boolean,
    onShareLink: () -> Unit,
    onShareFile: () -> Unit,
    onSaveFile: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = RyggTheme.dimens.commonContentPadding16)
        ) {
            Text(
                text = stringResource(R.string.details_share),
                style = RyggTheme.typography.titleMedium,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                modifier = Modifier.padding(
                    horizontal = RyggTheme.dimens.commonContentPadding24,
                    vertical = RyggTheme.dimens.commonContentPadding8
                )
            )
            ShareOption(
                icon = Icons.Outlined.Link,
                label = stringResource(
                    if (isSharingLink) R.string.details_share_link_preparing else R.string.details_share_link
                ),
                onClick = onShareLink,
                enabled = !isSharingLink,
                isLoading = isSharingLink
            )
            ShareOption(
                icon = Icons.Outlined.FileDownload,
                label = stringResource(R.string.details_share_file),
                onClick = onShareFile,
                enabled = !isSharingLink
            )
            ShareOption(
                icon = Icons.Outlined.SaveAlt,
                label = stringResource(R.string.details_save_file),
                onClick = onSaveFile,
                enabled = !isSharingLink
            )
        }
    }
}

@Composable
private fun ShareOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val dimmed = !enabled && !isLoading
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding24,
                vertical = RyggTheme.dimens.commonContentPadding16
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        Box(
            modifier = Modifier.size(RyggTheme.dimens.iconSize24),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = RyggTheme.getColor(RyggColor.BrandGreen),
                    strokeWidth = RyggTheme.dimens.border2,
                    modifier = Modifier.size(RyggTheme.dimens.progressIndicator20)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RyggTheme.getColor(if (dimmed) RyggColor.TextSecondary else RyggColor.BrandGreen),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                )
            }
        }
        Text(
            text = label,
            style = RyggTheme.typography.bodyLarge,
            color = RyggTheme.getColor(if (dimmed) RyggColor.TextSecondary else RyggColor.TextPrimary)
        )
    }
}

@Preview
@Composable
private fun DetailsShareSheetPreview() {
    RyggTheme {
        // ModalBottomSheet renders in a separate window; the preview shows the option rows.
        Column {
            ShareOption(icon = Icons.Outlined.Link, label = "Share link", onClick = {})
            ShareOption(icon = Icons.Outlined.FileDownload, label = "Share GPX file", onClick = {})
            ShareOption(icon = Icons.Outlined.SaveAlt, label = "Save to device", onClick = {})
        }
    }
}
