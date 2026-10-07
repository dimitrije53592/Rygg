package com.example.rygg.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggShapes
import com.example.rygg.core.ui.theme.RyggTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    // Must match the shape of whatever is swiped, or the red backdrop shows around its corners.
    shape: Shape = RyggShapes.card,
    content: @Composable () -> Unit
) {
    // The swipe only *requests* the delete: rejecting the state change snaps the row back, so a
    // cancelled confirmation leaves it in place instead of stranded off-screen.
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) onDelete()
            false
        }
    )
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = { DeleteBackground(shape) },
        content = { content() }
    )
}

@Composable
private fun DeleteBackground(shape: Shape) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(RyggTheme.getColor(RyggColor.Error))
            .padding(horizontal = RyggTheme.dimens.commonContentPadding20),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.OnBrand),
            modifier = Modifier.size(RyggTheme.dimens.iconSize24)
        )
        Text(
            text = stringResource(R.string.library_delete),
            color = RyggTheme.getColor(RyggColor.OnBrand),
            style = RyggTheme.typography.labelLarge
        )
    }
}
