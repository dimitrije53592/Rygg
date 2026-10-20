package com.dimitrijeilic.rygg.feature.details.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.LoadingIndicator
import com.dimitrijeilic.rygg.core.ui.components.RyggAsyncImage
import com.dimitrijeilic.rygg.core.ui.components.RyggPageDots
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.feature.library.domain.MAX_IMAGES_PER_ROUTE
import com.dimitrijeilic.rygg.feature.library.domain.RouteImage

@Composable
fun DetailsGalleryCard(
    images: List<RouteImage>,
    isAdding: Boolean,
    onAddPhotos: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onManagePhotos: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) {
        EmptyGallery(isAdding = isAdding, onAddPhotos = onAddPhotos, modifier = modifier)
        return
    }
    val isFull = images.size >= MAX_IMAGES_PER_ROUTE
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Slideshow(images = images, onOpenPhoto = onOpenPhoto)
        Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
            if (images.size > 1) {
                RyggSecondaryButton(
                    text = stringResource(R.string.details_photos_manage),
                    onClick = onManagePhotos,
                    modifier = Modifier.weight(1f)
                )
            }
            RyggSecondaryButton(
                text = stringResource(R.string.details_photos_add),
                onClick = onAddPhotos,
                isEnabled = !isFull && !isAdding,
                modifier = Modifier.weight(1f)
            )
        }
        if (isAdding || isFull) {
            Text(
                text = if (isAdding) {
                    stringResource(R.string.details_photos_adding)
                } else {
                    stringResource(R.string.details_photos_limit, MAX_IMAGES_PER_ROUTE)
                },
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        }
    }
}

@Composable
private fun Slideshow(
    images: List<RouteImage>,
    onOpenPhoto: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val scrim = RyggTheme.getColor(RyggColor.ScrimDark)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(GALLERY_ASPECT)
            .clip(RyggShapes.card)
    ) {
        HorizontalPager(
            state = pagerState,
            key = { page -> images[page].id },
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val image = images[page]
            RyggAsyncImage(
                uri = image.uri,
                isAvailable = image.isAvailable,
                contentDescription = stringResource(R.string.details_photo_description, page + 1),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = image.isAvailable) { onOpenPhoto(image.id) }
            )
        }
        if (images.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, scrim.copy(alpha = DOTS_SCRIM_ALPHA))))
                    .padding(RyggTheme.dimens.commonContentPadding12),
                contentAlignment = Alignment.Center
            ) {
                RyggPageDots(pageCount = images.size, currentPage = pagerState.currentPage)
            }
        }
    }
}

@Composable
private fun EmptyGallery(
    isAdding: Boolean,
    onAddPhotos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outline = RyggTheme.getColor(RyggColor.Outline)
    val shape = RyggShapes.card
    val strokeWidth = RyggTheme.dimens.border2
    val dash = RyggTheme.dimens.commonSpacing8
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(EMPTY_ASPECT)
            .clip(shape)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .drawBehind {
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = outline,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), dash.toPx()))
                    )
                )
            }
            .clickable(enabled = !isAdding, onClick = onAddPhotos),
        contentAlignment = Alignment.Center
    ) {
        if (isAdding) {
            LoadingIndicator(text = stringResource(R.string.details_photos_adding))
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = RyggTheme.getColor(RyggColor.BrandGreen),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize32)
                )
                Text(
                    text = stringResource(R.string.details_photos_add),
                    style = RyggTheme.typography.titleSmall,
                    color = RyggTheme.getColor(RyggColor.TextPrimary)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsGalleryCardPreview() {
    RyggTheme {
        Column(
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
        ) {
            DetailsGalleryCard(
                images = emptyList(),
                isAdding = false,
                onAddPhotos = {},
                onOpenPhoto = {},
                onManagePhotos = {}
            )
            DetailsGalleryCard(
                images = List(3) { index ->
                    RouteImage(
                        id = "image-$index",
                        entryId = 1L,
                        uri = Uri.EMPTY,
                        position = index,
                        width = 2048,
                        height = 1536,
                        isAvailable = false
                    )
                },
                isAdding = false,
                onAddPhotos = {},
                onOpenPhoto = {},
                onManagePhotos = {}
            )
        }
    }
}

private const val GALLERY_ASPECT = 4f / 3f
private const val EMPTY_ASPECT = 16f / 9f
private const val DOTS_SCRIM_ALPHA = 0.5f
