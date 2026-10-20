package com.dimitrijeilic.rygg.feature.library.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.pressScale
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggElevation
import com.dimitrijeilic.rygg.core.ui.theme.RyggMotion
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.theme.ryggElevation

@Composable
internal fun LibraryFolderCard(
    name: String,
    routeCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onRename: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RyggShapes.card
    val interactionSource = remember { MutableInteractionSource() }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) EXPANDED_ROTATION else 0f,
        animationSpec = if (RyggTheme.reduceMotion) RyggMotion.effectsFast() else RyggMotion.spatial(),
        label = "folderChevron"
    )
    val stateLabel = stringResource(if (expanded) R.string.library_folder_expanded else R.string.library_folder_collapsed)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .ryggElevation(level = RyggElevation.Card, shape = shape)
            .clip(shape)
            .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
            .then(
                if (RyggTheme.isDarkMode) {
                    Modifier.border(
                        width = RyggTheme.dimens.border1,
                        color = RyggTheme.getColor(RyggColor.Outline),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onLongClickLabel = stringResource(R.string.library_folder_rename),
                onLongClick = onRename,
                onClick = onToggle
            )
            .semantics { stateDescription = stateLabel }
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
    ) {
        Box(
            modifier = Modifier
                .size(RyggTheme.dimens.iconSize40)
                .clip(RyggShapes.chip)
                .background(RyggTheme.getColor(RyggColor.MossSurface)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_folder),
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.BrandGreen),
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = RyggTheme.typography.titleMedium,
                color = RyggTheme.getColor(RyggColor.TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(R.plurals.library_folder_routes, routeCount, routeCount),
                style = RyggTheme.typography.bodySmall,
                color = RyggTheme.getColor(RyggColor.TextSecondary)
            )
        }
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier
                .size(RyggTheme.dimens.iconSize24)
                .rotate(chevronRotation)
        )
    }
}

private const val EXPANDED_ROTATION = 180f

@Preview
@Composable
private fun LibraryFolderCardPreview() {
    RyggTheme {
        Column(
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
        ) {
            LibraryFolderCard(name = "Julian Alps", routeCount = 4, expanded = true, onToggle = {}, onRename = {})
            LibraryFolderCard(name = "Winter tours", routeCount = 1, expanded = false, onToggle = {}, onRename = {})
        }
    }
}
