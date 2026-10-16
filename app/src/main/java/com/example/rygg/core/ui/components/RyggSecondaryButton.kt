package com.example.rygg.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

// The quiet partner to RyggPrimaryButton: same size and shape, outlined, never brand-coloured, so
// each screen keeps a single primary action (and destructive choices never borrow the brand green).
@Composable
fun RyggSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    textColor: Color = RyggTheme.getColor(RyggColor.TextPrimary)
) {
    RyggPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        isEnabled = isEnabled,
        backgroundColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
        textColor = textColor,
        borderWidth = RyggTheme.dimens.border1,
        borderColor = RyggTheme.getColor(RyggColor.Outline)
    )
}
