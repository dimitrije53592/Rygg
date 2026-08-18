package com.example.rygg.feature.details.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun DetailsSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = RyggTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = RyggTheme.getColor(RyggColor.TextSecondary),
        modifier = modifier.padding(horizontal = RyggTheme.dimens.commonContentPadding4)
    )
}
