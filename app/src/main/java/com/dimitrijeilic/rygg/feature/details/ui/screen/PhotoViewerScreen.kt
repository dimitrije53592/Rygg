package com.dimitrijeilic.rygg.feature.details.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggPageDots
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.feature.details.ui.components.DeletePhotoDialog
import com.dimitrijeilic.rygg.feature.details.ui.components.ZoomableImage
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.PhotoViewerLoadingState
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.PhotoViewerUiState
import com.dimitrijeilic.rygg.feature.library.domain.RouteImage

@Composable
fun PhotoViewerScreen(params: PhotoViewerScreenParams) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RyggTheme.getColor(RyggColor.ScrimDark))
    ) {
        when (val state = params.uiState.loadingState) {
            PhotoViewerLoadingState.Loading -> Unit

            PhotoViewerLoadingState.Error ->
                Text(
                    text = stringResource(R.string.details_not_found),
                    style = RyggTheme.typography.titleMedium,
                    color = RyggTheme.getColor(RyggColor.OnScrim),
                    modifier = Modifier.align(Alignment.Center)
                )

            is PhotoViewerLoadingState.Loaded ->
                if (state.images.isNotEmpty()) {
                    LoadedContent(images = state.images, params = params)
                }
        }
        ViewerTopBar(onNavigateBack = params.onNavigateBack)
    }
}

@Composable
private fun LoadedContent(
    images: List<RouteImage>,
    params: PhotoViewerScreenParams
) {
    val startPage = remember { images.indexOfFirst { it.id == params.uiState.startImageId }.coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { images.size })
    var isZoomed by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<RouteImage?>(null) }
    val current = images.getOrNull(pagerState.currentPage)

    HorizontalPager(
        state = pagerState,
        key = { page -> images[page].id },
        userScrollEnabled = !isZoomed,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val image = images[page]
        ZoomableImage(
            uri = image.uri,
            isAvailable = image.isAvailable,
            contentDescription = stringResource(R.string.details_photo_description, page + 1),
            onZoomedChange = { zoomed -> if (page == pagerState.currentPage) isZoomed = zoomed }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(RyggTheme.dimens.commonContentPadding16)
    ) {
        if (images.size > 1) {
            RyggPageDots(
                pageCount = images.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (current != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(RyggTheme.dimens.commonContentPadding12),
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8, Alignment.End)
        ) {
            val isCover = current.position == 0
            ViewerAction(
                icon = if (isCover) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = stringResource(
                    if (isCover) R.string.photo_viewer_is_cover else R.string.photo_viewer_set_cover
                ),
                enabled = !isCover,
                onClick = { params.onSetCover(current.id) }
            )
            ViewerAction(
                icon = Icons.Outlined.DeleteOutline,
                contentDescription = stringResource(R.string.photo_viewer_delete),
                onClick = { pendingDelete = current }
            )
        }
    }

    pendingDelete?.let { image ->
        DeletePhotoDialog(
            onConfirm = {
                pendingDelete = null
                params.onDeletePhoto(image.id)
            },
            onCancel = { pendingDelete = null }
        )
    }
}

@Composable
private fun ViewerTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .padding(RyggTheme.dimens.commonContentPadding12)
    ) {
        ViewerAction(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.nav_back),
            onClick = onNavigateBack
        )
    }
}

@Composable
private fun ViewerAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(RyggTheme.dimens.iconSize48)
            .clip(RyggShapes.medium)
            .background(RyggTheme.getColor(RyggColor.OnScrim).copy(alpha = GLASS_ALPHA))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) {
                RyggTheme.getColor(RyggColor.OnScrim)
            } else {
                RyggTheme.getColor(RyggColor.AccentBright)
            },
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
    }
}

@Preview
@Composable
private fun PhotoViewerScreenPreview() {
    RyggTheme {
        PhotoViewerScreen(
            params = PhotoViewerScreenParams(
                uiState = PhotoViewerUiState(
                    startImageId = "image-0",
                    loadingState = PhotoViewerLoadingState.Loaded(
                        List(3) { index ->
                            RouteImage(
                                id = "image-$index",
                                entryId = 1L,
                                uri = Uri.EMPTY,
                                position = index,
                                width = 2048,
                                height = 1536,
                                isAvailable = false
                            )
                        }
                    )
                ),
                onNavigateBack = {},
                onSetCover = {},
                onDeletePhoto = {}
            )
        )
    }
}

private const val GLASS_ALPHA = 0.16f

data class PhotoViewerScreenParams(
    val uiState: PhotoViewerUiState,
    val onNavigateBack: () -> Unit,
    val onSetCover: (String) -> Unit,
    val onDeletePhoto: (String) -> Unit
)
