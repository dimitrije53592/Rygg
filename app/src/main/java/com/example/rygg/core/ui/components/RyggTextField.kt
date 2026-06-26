package com.example.rygg.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    singleLine: Boolean = true,
    labelText: String? = null,
    labelTextColor: Color = RyggTheme.getColor(RyggColor.TextSecondary),
    placeholderText: String? = null,
    placeholderTextColor: Color = RyggTheme.getColor(RyggColor.OnBrandLightGray),
    leadingIcon: Painter? = null,
    leadingIconTint: Color = RyggTheme.getColor(RyggColor.TextSecondary),
    trailingIcon: Painter? = null,
    trailingIconTint: Color = RyggTheme.getColor(RyggColor.TextSecondary)
) {
    val isFocused = remember { mutableStateOf(false) }

    Column {
        labelText?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RyggTheme.dimens.commonContentPadding4),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = labelText,
                    color = labelTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            enabled = isEnabled,
            singleLine = singleLine,
            placeholder = placeholderText?.let {
                {
                    Text(
                        text = placeholderText,
                        color = placeholderTextColor
                    )
                }
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        painter = it,
                        tint = leadingIconTint,
                        contentDescription = ""
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    Icon(
                        painter = trailingIcon,
                        tint = trailingIconTint,
                        contentDescription = ""
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = RyggTheme.getColor(RyggColor.TextPrimary),
                unfocusedTextColor = RyggTheme.getColor(RyggColor.TextPrimary),
                focusedContainerColor = RyggTheme.getColor(RyggColor.BackgroundWhite),
                unfocusedContainerColor = RyggTheme.getColor(RyggColor.SurfaceGray),
                focusedPlaceholderColor = RyggTheme.getColor(RyggColor.OnBrandLightGray),
                unfocusedPlaceholderColor = RyggTheme.getColor(RyggColor.OnBrandLightGray),
                focusedBorderColor = RyggTheme.getColor(RyggColor.BrandGreen),
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(RyggTheme.dimens.radius12),
            modifier = modifier.onFocusChanged {
                isFocused.value = it.isFocused
            }
        )
    }
}

@Preview
@Composable
fun RyggTextFieldPreview() {
    RyggTheme {
        RyggTextField(
            value = "Button text",
            onValueChange = {}
        )
    }
}