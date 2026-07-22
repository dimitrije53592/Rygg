package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
internal fun LibraryEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.library_empty_title),
                style = RyggTheme.typography.titleMedium,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
            Text(
                text = stringResource(R.string.library_empty_subtitle),
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun LibraryErrorState(errorMessage: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = RyggTheme.dimens.commonContentPadding16)
        ) {
            Text(
                text = errorMessage.orEmpty(),
                style = RyggTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
        }
    }
}

@Composable
internal fun LibraryLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = RyggTheme.getColor(RyggColor.BrandGreen))
    }
}

@Composable
internal fun LibraryNoMatchesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.library_no_matches),
            style = RyggTheme.typography.bodyMedium,
            color = RyggTheme.getColor(RyggColor.TextSecondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = RyggTheme.dimens.commonContentPadding16)
        )
    }
}
