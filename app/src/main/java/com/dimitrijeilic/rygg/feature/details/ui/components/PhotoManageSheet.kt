package com.dimitrijeilic.rygg.feature.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggAsyncImage
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggElevation
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.theme.ryggElevation
import com.dimitrijeilic.rygg.core.ui.utils.rememberRyggHaptics
import com.dimitrijeilic.rygg.feature.library.domain.RouteImage
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

// Long-press a tile and drag it; the order is committed once, when the drag ends, so a reorder
// costs one sync push instead of one per swap.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoManageSheet(
    images: List<RouteImage>,
    onReorder: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var order by remember(images) { mutableStateOf(images) }
    val haptics = rememberRyggHaptics()
    val gridState = rememberLazyGridState()
    val reorderState = rememberReorderableLazyGridState(gridState) { from, to ->
        order = order.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shape = RyggShapes.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = RyggTheme.dimens.commonContentPadding24,
                    vertical = RyggTheme.dimens.commonContentPadding12
                ),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            Text(
                text = stringResource(R.string.photo_manage_title),
                style = RyggTheme.typography.titleLarge,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            Text(
                text = stringResource(R.string.photo_manage_hint),
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8),
                verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8),
                modifier = Modifier.padding(top = RyggTheme.dimens.commonContentPadding8)
            ) {
                items(order, key = { it.id }) { image ->
                    ReorderableItem(reorderState, key = image.id) { isDragging ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .longPressDraggableHandle(
                                    onDragStarted = { haptics.longPress() },
                                    onDragStopped = {
                                        val ids = order.map { it.id }
                                        if (ids != images.map { it.id }) onReorder(ids)
                                    }
                                )
                                .ryggElevation(
                                    level = if (isDragging) RyggElevation.Floating else RyggElevation.None,
                                    shape = RyggShapes.thumbnail
                                )
                                .clip(RyggShapes.thumbnail)
                        ) {
                            RyggAsyncImage(
                                uri = image.uri,
                                isAvailable = image.isAvailable,
                                contentDescription = null,
                                showStatusLabel = false,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (order.firstOrNull()?.id == image.id) {
                                CoverBadge(modifier = Modifier.align(Alignment.BottomStart))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.photo_cover).uppercase(),
        style = RyggTheme.textStyles.trackedLabel,
        color = RyggTheme.getColor(RyggColor.OnBrand),
        modifier = modifier
            .padding(RyggTheme.dimens.commonContentPadding4)
            .clip(RyggShapes.extraSmall)
            .background(RyggTheme.getColor(RyggColor.BrandGreen))
            .padding(
                horizontal = RyggTheme.dimens.commonContentPadding8,
                vertical = RyggTheme.dimens.commonContentPadding4
            )
    )
}

private const val GRID_COLUMNS = 3
