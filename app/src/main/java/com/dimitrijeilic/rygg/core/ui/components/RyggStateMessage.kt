package com.dimitrijeilic.rygg.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggStateMessage(
    icon: ImageVector,
    title: String,
    body: String?,
    tint: Color = RyggTheme.getColor(RyggColor.TextSecondary),
    action: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding32),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            Box(
                modifier = Modifier
                    .size(RyggTheme.dimens.iconSize64)
                    .clip(RyggShapes.card)
                    .background(RyggTheme.getColor(RyggColor.MossSurface)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(RyggTheme.dimens.iconSize32)
                )
            }
            Text(
                text = title,
                style = RyggTheme.typography.titleLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                textAlign = TextAlign.Center
            )
            body?.let {
                Text(
                    text = it,
                    style = RyggTheme.typography.bodyMedium,
                    color = RyggTheme.getColor(RyggColor.TextSecondary),
                    textAlign = TextAlign.Center
                )
            }
            action?.invoke()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RyggStateMessagePreview() {
    RyggTheme {
        RyggStateMessage(
            icon = Icons.Outlined.Map,
            title = "No routes yet",
            body = "Import a .gpx file to get started."
        )
    }
}
