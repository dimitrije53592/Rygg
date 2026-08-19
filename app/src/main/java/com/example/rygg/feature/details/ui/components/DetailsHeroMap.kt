package com.example.rygg.feature.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.ui.components.TrailThumbnail

@Composable
fun DetailsHeroMap(
    entry: GpxFileEntry,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    sourceLabel: String? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onShareLink: (() -> Unit)? = null,
    onShareFile: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .size(RyggTheme.dimens.detailsHeroHeight)
    ) {
        TrailThumbnail(
            points = entry.pathPoints,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.28f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(RyggTheme.dimens.commonContentPadding12),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlassIconButton(
                onClick = onNavigateBack,
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back),
                        tint = Color.White,
                        modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                    )
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
                if (onShareLink != null && onShareFile != null) {
                    HeroShareButton(
                        onShareLink = onShareLink,
                        onShareFile = onShareFile
                    )
                }
                if (onToggleFavorite != null && onDelete != null) {
                    HeroOverflowMenu(
                        isFavorite = entry.isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        onDelete = onDelete
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
                DisciplineChip(entry.discipline)
                sourceLabel?.let { SourceChip(label = it) }
            }
            Text(
                text = entry.name,
                style = RyggTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroShareButton(
    onShareLink: () -> Unit,
    onShareFile: () -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    GlassIconButton(
        onClick = { showSheet = true },
        content = {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(R.string.details_share),
                tint = Color.White,
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
    )
    if (showSheet) {
        DetailsShareSheet(
            onShareLink = {
                showSheet = false
                onShareLink()
            },
            onShareFile = {
                showSheet = false
                onShareFile()
            },
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
private fun HeroOverflowMenu(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        GlassIconButton(
            onClick = { expanded = true },
            content = {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.details_more_actions),
                    tint = Color.White,
                    modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                )
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            if (isFavorite) R.string.details_unfavorite else R.string.details_favorite
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = RyggTheme.getColor(RyggColor.BrandGreen)
                    )
                },
                onClick = {
                    expanded = false
                    onToggleFavorite()
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.details_delete)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = RyggTheme.getColor(RyggColor.Error)
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.buttonSize40)
            .clip(RoundedCornerShape(RyggTheme.dimens.radius12))
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun SourceChip(label: String) {
    Text(
        text = label,
        style = RyggTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
            .background(Color.Black.copy(alpha = 0.32f))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding8,
                vertical = RyggTheme.dimens.commonContentPadding4
            )
    )
}

@Composable
private fun DisciplineChip(discipline: Discipline) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
            .background(RyggTheme.getColor(RyggColor.BrandGreen))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding8,
                vertical = RyggTheme.dimens.commonContentPadding4
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
    ) {
        Icon(
            painter = painterResource(discipline.iconRes),
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.OnBrand),
            modifier = Modifier.size(RyggTheme.dimens.iconSize16)
        )
        Text(
            text = discipline.name.capitalize(),
            style = RyggTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = RyggTheme.getColor(RyggColor.OnBrand)
        )
    }
}
