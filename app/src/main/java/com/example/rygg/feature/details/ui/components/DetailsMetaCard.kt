package com.example.rygg.feature.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.library.domain.GpxFileEntry

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsMetaCard(
    entry: GpxFileEntry,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RyggTheme.dimens.radius16))
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        if (entry.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8),
                verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
            ) {
                entry.tags.forEach { tag -> TagChip(tag) }
            }
        }
        if (entry.description.isNotBlank()) {
            Text(
                text = entry.description,
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        }
    }
}

@Composable
private fun TagChip(tag: String) {
    Text(
        text = tag,
        style = RyggTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = RyggTheme.getColor(RyggColor.BrandGreen),
        modifier = Modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
            .background(RyggTheme.getColor(RyggColor.MossSurface))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding12,
                vertical = RyggTheme.dimens.commonContentPadding8
            )
    )
}
