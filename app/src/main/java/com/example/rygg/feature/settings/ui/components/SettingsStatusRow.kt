package com.example.rygg.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.core.ui.components.RyggCard
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun SettingsStatusRow(
    label: String,
    status: String,
    isOk: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = RyggTheme.dimens.commonContentPadding4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = RyggTheme.typography.bodyLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            Text(
                text = status,
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(if (isOk) RyggColor.BrandGreen else RyggColor.Error)
            )
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
