package com.example.rygg.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggNavigationRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RyggCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RyggTheme.getColor(RyggColor.TextSecondary),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                )
                Text(
                    text = label,
                    style = RyggTheme.typography.bodyLarge,
                    color = RyggTheme.getColor(RyggColor.TextPrimary)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
    }
}

@Preview
@Composable
private fun RyggNavigationRowPreview() {
    RyggTheme {
        RyggNavigationRow(
            label = "Settings",
            icon = Icons.Outlined.Settings,
            onClick = {}
        )
    }
}
