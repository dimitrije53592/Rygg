package com.dimitrijeilic.rygg.feature.offline.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggCard
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggStat
import com.dimitrijeilic.rygg.core.ui.components.RyggStatSize
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.core.ui.utils.formatDate
import com.dimitrijeilic.rygg.core.units.rememberUnitFormatter
import com.dimitrijeilic.rygg.feature.offline.domain.OfflineRegion
import com.dimitrijeilic.rygg.feature.offline.domain.RegionBounds
import com.dimitrijeilic.rygg.feature.offline.domain.RegionStatus

@Composable
internal fun OfflineRegionCard(
    region: OfflineRegion,
    onOpen: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onUpdate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    canStartDownload: Boolean = true
) {
    val units = rememberUnitFormatter()
    val isComplete = region.status == RegionStatus.Complete

    RyggCard(
        modifier = modifier,
        onClick = if (isComplete) onOpen else null
    ) {
        Row(
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
                    imageVector = if (isComplete) Icons.Outlined.DownloadDone else Icons.Outlined.Download,
                    contentDescription = null,
                    tint = RyggTheme.getColor(RyggColor.BrandGreen),
                    modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = region.name.ifBlank { stringResource(R.string.offline_unnamed) },
                    style = RyggTheme.typography.titleMedium,
                    color = RyggTheme.getColor(RyggColor.TextPrimary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = statusText(region),
                    style = RyggTheme.typography.bodySmall,
                    color = RyggTheme.getColor(
                        if (region.status == RegionStatus.Failed) RyggColor.Error else RyggColor.TextSecondary
                    )
                )
            }
            RegionOverflowMenu(
                status = region.status,
                canStartDownload = canStartDownload,
                onPause = onPause,
                onUpdate = onUpdate,
                onRename = onRename,
                onDelete = onDelete
            )
        }
        Spacer(modifier = Modifier.height(RyggTheme.dimens.commonSpacing16))
        Row(horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)) {
            RyggStat(
                quantity = units.fileSizeParts(region.sizeBytes),
                label = stringResource(R.string.offline_region_size),
                size = RyggStatSize.Small
            )
            RyggStat(
                quantity = units.areaParts(region.bounds.areaSquareMeters()),
                label = stringResource(R.string.offline_region_area),
                size = RyggStatSize.Small
            )
        }
        if (!isComplete) {
            Spacer(modifier = Modifier.height(RyggTheme.dimens.commonSpacing16))
            LinearProgressIndicator(
                progress = { region.progress },
                color = RyggTheme.getColor(
                    if (region.status == RegionStatus.Failed) RyggColor.Error else RyggColor.BrandGreen
                ),
                trackColor = RyggTheme.getColor(RyggColor.MossSurface),
                drawStopIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RyggShapes.chip)
            )
        }
        if (region.status == RegionStatus.Paused || region.status == RegionStatus.Failed) {
            Spacer(modifier = Modifier.height(RyggTheme.dimens.commonSpacing16))
            RyggSecondaryButton(
                text = stringResource(
                    if (region.status == RegionStatus.Failed) R.string.offline_region_retry else R.string.offline_region_resume
                ),
                onClick = onResume,
                isEnabled = canStartDownload,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun statusText(region: OfflineRegion): String {
    val percent = (region.progress * PERCENT).toInt()
    return when (region.status) {
        RegionStatus.Downloading -> stringResource(R.string.offline_region_downloading, percent)
        RegionStatus.Paused -> stringResource(R.string.offline_region_paused, percent)
        RegionStatus.Failed -> stringResource(R.string.offline_region_failed, percent)
        RegionStatus.Complete -> if (region.updatedAt > region.createdAt) {
            stringResource(R.string.offline_region_updated, formatDate(region.updatedAt))
        } else {
            stringResource(R.string.offline_region_downloaded, formatDate(region.createdAt))
        }
    }
}

@Composable
private fun RegionOverflowMenu(
    status: RegionStatus,
    canStartDownload: Boolean,
    onPause: () -> Unit,
    onUpdate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.offline_region_more),
            tint = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier
                .clip(RyggShapes.chip)
                .clickable { expanded = true }
                .padding(RyggTheme.dimens.commonContentPadding4)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (status == RegionStatus.Downloading) {
                RegionMenuItem(icon = Icons.Outlined.Pause, label = R.string.offline_region_pause) {
                    expanded = false
                    onPause()
                }
            }
            if (status == RegionStatus.Complete && canStartDownload) {
                RegionMenuItem(icon = Icons.Outlined.Refresh, label = R.string.offline_region_update) {
                    expanded = false
                    onUpdate()
                }
            }
            RegionMenuItem(icon = Icons.Outlined.Edit, label = R.string.offline_region_rename) {
                expanded = false
                onRename()
            }
            RegionMenuItem(icon = Icons.Outlined.Delete, label = R.string.offline_region_delete, tint = RyggColor.Error) {
                expanded = false
                onDelete()
            }
        }
    }
}

@Composable
private fun RegionMenuItem(
    icon: ImageVector,
    @StringRes label: Int,
    tint: RyggColor = RyggColor.BrandGreen,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = stringResource(label)) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RyggTheme.getColor(tint)
            )
        },
        onClick = onClick
    )
}

private const val PERCENT = 100

@Preview
@Composable
private fun OfflineRegionCardPreview() {
    val bounds = RegionBounds(south = 46.55, west = 7.95, north = 46.73, east = 8.21)
    RyggTheme {
        Column(
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
        ) {
            OfflineRegionCard(
                region = OfflineRegion(
                    id = 1L,
                    name = "Jungfrau region",
                    bounds = bounds,
                    createdAt = 1_762_300_000_000,
                    updatedAt = 1_762_300_000_000,
                    sourceRouteId = null,
                    sizeBytes = 38_000_000,
                    progress = 1f,
                    status = RegionStatus.Complete
                ),
                onOpen = {},
                onResume = {},
                onPause = {},
                onUpdate = {},
                onRename = {},
                onDelete = {}
            )
            OfflineRegionCard(
                region = OfflineRegion(
                    id = 2L,
                    name = "Triglav",
                    bounds = bounds,
                    createdAt = 1_762_300_000_000,
                    updatedAt = 1_762_300_000_000,
                    sourceRouteId = 4L,
                    sizeBytes = 12_000_000,
                    progress = 0.42f,
                    status = RegionStatus.Paused
                ),
                onOpen = {},
                onResume = {},
                onPause = {},
                onUpdate = {},
                onRename = {},
                onDelete = {}
            )
        }
    }
}
