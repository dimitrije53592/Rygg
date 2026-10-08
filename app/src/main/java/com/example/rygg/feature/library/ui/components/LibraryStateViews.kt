package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.shimmer
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggShapes
import com.example.rygg.core.ui.theme.RyggTheme

@Composable
internal fun LibraryEmptyState(onImport: () -> Unit) {
    StateMessage(
        icon = Icons.Outlined.Map,
        title = stringResource(R.string.library_empty_title),
        body = stringResource(R.string.library_empty_subtitle)
    ) {
        RyggPrimaryButton(
            text = stringResource(R.string.library_empty_action),
            onClick = onImport
        )
    }
}

@Composable
internal fun LibraryErrorState(errorMessage: String?) {
    StateMessage(
        icon = Icons.Outlined.ErrorOutline,
        tint = RyggTheme.getColor(RyggColor.Error),
        title = stringResource(R.string.library_error_title),
        body = errorMessage?.takeIf { it.isNotBlank() } ?: stringResource(R.string.library_error_body)
    )
}

@Composable
internal fun LibraryNoMatchesState() {
    StateMessage(
        icon = Icons.Outlined.SearchOff,
        title = stringResource(R.string.library_no_matches),
        body = null
    )
}

// Shaped like the cards that are coming, so the list does not jump when they land.
@Composable
internal fun LibraryLoadingState() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = RyggTheme.dimens.commonContentPadding16,
            end = RyggTheme.dimens.commonContentPadding16,
            top = RyggTheme.dimens.commonContentPadding4,
            bottom = RyggTheme.dimens.commonContentPadding80
        ),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16),
        userScrollEnabled = false
    ) {
        items(SKELETON_CARD_COUNT) {
            SkeletonCard()
        }
    }
}

@Composable
private fun SkeletonCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RyggShapes.card)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CARD_ASPECT)
                .shimmer()
        )
        Row(
            modifier = Modifier.padding(
                horizontal = RyggTheme.dimens.commonContentPadding16,
                vertical = RyggTheme.dimens.commonContentPadding16
            ),
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)
        ) {
            repeat(SKELETON_CARD_COUNT) {
                Box(
                    modifier = Modifier
                        .width(RyggTheme.dimens.iconSize64)
                        .height(RyggTheme.dimens.iconSize16)
                        .clip(RyggShapes.chip)
                        .shimmer()
                )
            }
        }
    }
}

@Composable
private fun StateMessage(
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

private const val SKELETON_CARD_COUNT = 3
private const val CARD_ASPECT = 16f / 9f
