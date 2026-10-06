package com.example.rygg.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

/**
 * Shared dialog shell: the same elevated card the auth screens use, with an optional icon badge,
 * a title and a centred message. Callers fill the slot with their own body and action buttons so
 * the action hierarchy stays explicit at each call site.
 */
@Composable
fun RyggDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = RyggTheme.getColor(RyggColor.BrandGreen),
    iconBackgroundColor: Color = RyggTheme.getColor(RyggColor.MossSurface),
    message: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = RyggTheme.dimens.commonContentPadding24)
                .fillMaxWidth()
                .clip(RoundedCornerShape(RyggTheme.dimens.radius24))
                .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
                .verticalScroll(rememberScrollState())
                .padding(RyggTheme.dimens.commonContentPadding24),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(RyggTheme.dimens.iconSize48)
                        .clip(RoundedCornerShape(RyggTheme.dimens.radius12))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                    )
                }
            }
            Text(
                text = title,
                style = RyggTheme.typography.headlineSmall,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                textAlign = TextAlign.Center
            )
            message?.let {
                Text(
                    text = it,
                    style = RyggTheme.typography.bodyMedium,
                    color = RyggTheme.getColor(RyggColor.TextSecondary),
                    textAlign = TextAlign.Center
                )
            }
            content()
        }
    }
}
