package com.example.rygg.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.core.ui.components.RyggCard
import com.example.rygg.core.ui.components.RyggIconBadge
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun SettingsStatusRow(
    label: String,
    status: String,
    isOk: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = RyggTheme.dimens.commonContentPadding4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            leadingIcon?.let { RyggIconBadge(icon = it) }
            Column {
                Text(
                    text = label,
                    style = RyggTheme.typography.bodyLarge,
                    color = RyggTheme.getColor(RyggColor.TextPrimary)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
                ) {
                    val statusColor = RyggTheme.getColor(if (isOk) RyggColor.BrandGreen else RyggColor.Error)
                    Icon(
                        imageVector = if (isOk) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(RyggTheme.dimens.iconSize16)
                    )
                    Text(
                        text = status,
                        style = RyggTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
            }
        }
        TextButton(onClick = onAction) {
            Text(
                text = actionLabel,
                style = RyggTheme.typography.labelLarge,
                color = RyggTheme.getColor(RyggColor.BrandGreen)
            )
        }
    }
}

@Preview
@Composable
private fun SettingsStatusRowPreview() {
    RyggTheme {
        RyggCard {
            SettingsStatusRow(
                label = "Location",
                status = "Approximate only",
                isOk = false,
                actionLabel = "Manage",
                onAction = {}
            )
        }
    }
}
