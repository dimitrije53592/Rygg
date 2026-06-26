package com.example.rygg.feature.details.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
fun DetailsScreen(params: DetailsScreenParams) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding16),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Details for #${params.uiState.id}", style = RyggTheme.typography.titleLarge)
    }
}

data class DetailsUiState(
    val id: String
)

data class DetailsScreenParams(
    val uiState: DetailsUiState
)
