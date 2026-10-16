package com.example.rygg.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggRadioRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(vertical = RyggTheme.dimens.commonContentPadding8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = RyggTheme.getColor(RyggColor.BrandGreen),
                unselectedColor = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        )
        Column {
            Text(
                text = label,
                style = RyggTheme.typography.bodyLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            description?.let {
                Text(
                    text = it,
                    style = RyggTheme.typography.bodySmall,
                    color = RyggTheme.getColor(RyggColor.TextSecondary)
                )
            }
        }
    }
}

@Preview
@Composable
private fun RyggRadioRowPreview() {
    RyggTheme {
        RyggRadioRow(label = "Metric (km, m)", selected = true, onSelect = {})
    }
}
