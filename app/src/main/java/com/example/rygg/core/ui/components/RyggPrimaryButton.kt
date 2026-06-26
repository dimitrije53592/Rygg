package com.example.rygg.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun RyggPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(RyggTheme.dimens.progressIndicator20),
                color = LocalContentColor.current
            )
        } else {
            Text(text)
        }
    }
}
