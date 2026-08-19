package com.example.rygg.feature.details.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsShareSheet(
    onShareLink: () -> Unit,
    onShareFile: () -> Unit,
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
                fontWeight = FontWeight.SemiBold,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                modifier = Modifier.padding(
                    horizontal = RyggTheme.dimens.commonContentPadding24,
                    vertical = RyggTheme.dimens.commonContentPadding8
                )
            )
            ShareOption(
                icon = Icons.Default.Link,
                label = stringResource(R.string.details_share_link),
                onClick = onShareLink
            )
            ShareOption(
                icon = Icons.Default.FileDownload,
                label = stringResource(R.string.details_share_file),
                onClick = onShareFile
            )
        }
    }
}

@Composable
private fun ShareOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding24,
                vertical = RyggTheme.dimens.commonContentPadding16
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.BrandGreen),
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
        Text(
            text = label,
            style = RyggTheme.typography.bodyLarge,
            color = RyggTheme.getColor(RyggColor.TextPrimary)
        )
    }
}

@Preview
@Composable
private fun DetailsShareSheetPreview() {
    RyggTheme {
        // ModalBottomSheet renders in a separate window; the preview shows the option rows.
        Column {
            ShareOption(icon = Icons.Default.Link, label = "Share link", onClick = {})
            ShareOption(icon = Icons.Default.FileDownload, label = "Share GPX file", onClick = {})
        }
    }
}
