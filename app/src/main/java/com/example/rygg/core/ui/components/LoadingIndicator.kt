package com.example.rygg.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun LoadingIndicator(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = RyggTheme.getColor(RyggColor.BrandGreen))
        Spacer(Modifier.size(RyggTheme.dimens.commonSpacing12))
        Text(
            text = text,
            style = RyggTheme.typography.bodyMedium,
            color = RyggTheme.getColor(RyggColor.TextSecondary),
            textAlign = TextAlign.Center
        )
    }
}
