package com.example.rygg.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    backgroundColor: Color = RyggTheme.getColor(RyggColor.BrandGreen),
    progressIndicatorColor: Color = RyggTheme.getColor(RyggColor.OnBrand),
    textColor: Color = RyggTheme.getColor(RyggColor.OnBrand),
    leadingIcon: Painter? = null,
    leadingIconTint: Color = RyggTheme.getColor(RyggColor.OnBrand),
    buttonHeight: Dp = RyggTheme.dimens.buttonSize50,
    borderWidth: Dp = RyggTheme.dimens.border0,
    borderColor: Color = RyggTheme.getColor(RyggColor.Surface)
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = modifier.height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(RyggTheme.dimens.progressIndicator20),
                color = progressIndicatorColor
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let {
                    Icon(
                        painter = leadingIcon,
                        tint = leadingIconTint,
                        contentDescription = "",
                        modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                    )
                }
                Spacer(Modifier.size(RyggTheme.dimens.commonSpacing8))
                Text(
                    text = text,
                    color = textColor,
                    style = RyggTheme.typography.titleMedium
                )
            }
        }
    }
}
