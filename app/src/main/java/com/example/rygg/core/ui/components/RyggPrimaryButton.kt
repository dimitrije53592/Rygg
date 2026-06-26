package com.example.rygg.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    progressIndicatorColor: Color = RyggTheme.getColor(RyggColor.BackgroundWhite),
    textColor: Color = RyggTheme.getColor(RyggColor.BackgroundWhite),
    leadingIcon: Painter? = null,
    leadingIconTint: Color = RyggTheme.getColor(RyggColor.BackgroundWhite),
    buttonHeight: Dp = RyggTheme.dimens.buttonHeight50
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = modifier.height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(RyggTheme.dimens.progressIndicator20),
                color = progressIndicatorColor
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                leadingIcon?.let {
                    Icon(
                        painter = leadingIcon,
                        tint = leadingIconTint,
                        contentDescription = ""
                    )
                }
                Text(
                    text = text,
                    color = textColor
                )
            }
        }
    }
}
