package com.dimitrijeilic.rygg.feature.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.core.ui.components.RyggCard
import com.dimitrijeilic.rygg.core.ui.components.RyggIconBadge
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
fun SettingsLinkRow(
    label: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = RyggTheme.dimens.listRowHeight)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        RyggIconBadge(icon = leadingIcon)
        Text(
            text = label,
            style = RyggTheme.typography.bodyLarge,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
    }
}

@Preview
@Composable
private fun SettingsLinkRowPreview() {
    RyggTheme {
        RyggCard {
            SettingsLinkRow(
                label = "Licences",
                leadingIcon = Icons.Outlined.Description,
                onClick = {}
            )
        }
    }
}
