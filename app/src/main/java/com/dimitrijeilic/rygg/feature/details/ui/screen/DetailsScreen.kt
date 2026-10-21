package com.dimitrijeilic.rygg.feature.details.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.gpx.model.ElevationSample
import com.dimitrijeilic.rygg.core.ui.components.RyggPrimaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggSecondaryButton
import com.dimitrijeilic.rygg.core.ui.components.RyggTextField
import com.dimitrijeilic.rygg.core.ui.components.shimmer
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggShapes
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.feature.details.ui.components.DetailsContentsCard
import com.dimitrijeilic.rygg.feature.details.ui.components.DetailsGalleryCard
import com.dimitrijeilic.rygg.feature.details.ui.components.DetailsHeroMap
import com.dimitrijeilic.rygg.feature.details.ui.components.DetailsMetaCard
import com.dimitrijeilic.rygg.feature.details.ui.components.DetailsSectionTitle
import com.dimitrijeilic.rygg.feature.details.ui.components.DetailsStatsGrid
import com.dimitrijeilic.rygg.feature.details.ui.components.ElevationProfile
import com.dimitrijeilic.rygg.feature.details.ui.components.FolderPickerSheet
import com.dimitrijeilic.rygg.feature.details.ui.components.PhotoManageSheet
import com.dimitrijeilic.rygg.feature.details.ui.paramproviders.DetailsParamsProvider
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.DetailsLoadingState
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.DetailsUiState
import com.dimitrijeilic.rygg.feature.library.domain.EntrySource
import com.dimitrijeilic.rygg.feature.library.domain.GpxFileEntry

@Composable
fun DetailsScreen(params: DetailsScreenParams) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RyggTheme.getColor(RyggColor.SurfaceDim))
    ) {
        when (val state = params.uiState.loadingState) {
            is DetailsLoadingState.Loading -> DetailsSkeleton()

            is DetailsLoadingState.Error ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.message ?: stringResource(R.string.details_not_found),
                        style = RyggTheme.typography.titleMedium,
                        color = RyggTheme.getColor(RyggColor.TextPrimary),
                        modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16)
                    )
                }

            is DetailsLoadingState.Loaded ->
                LoadedContent(
                    entry = state.entry,
                    elevationProfile = state.elevationProfile,
                    params = params
                )

            is DetailsLoadingState.Empty ->
                EmptyRecordingContent(onDiscard = params.onNavigateBack)
        }
    }
}

// Graceful state for a recording that captured too little to save: a notice plus a quick
// confirm-to-discard, instead of a bare error. Discard reuses the screen's back/discard action.
@Composable
private fun EmptyRecordingContent(onDiscard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.record_empty_title),
            style = RyggTheme.typography.titleLarge,
            color = RyggTheme.getColor(RyggColor.TextPrimary),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.record_empty_message),
            style = RyggTheme.typography.bodyMedium,
            color = RyggTheme.getColor(RyggColor.TextSecondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = RyggTheme.dimens.commonSpacing8)
        )
        RyggPrimaryButton(
            text = stringResource(R.string.record_empty_confirm_discard),
            onClick = { onDiscard() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = RyggTheme.dimens.commonContentPadding16)
        )
    }
}

@Composable
private fun LoadedContent(
    entry: GpxFileEntry,
    elevationProfile: List<ElevationSample>,
    params: DetailsScreenParams
) {
    val mode = params.mode
    val sourceLabel = when (mode) {
        is DetailsMode.SavePreview -> when (entry.source) {
            EntrySource.IMPORTED -> stringResource(R.string.details_import_preview)
            EntrySource.RECORDED -> stringResource(R.string.details_recording_preview)
        }
        is DetailsMode.View -> when (entry.source) {
            EntrySource.IMPORTED -> stringResource(R.string.details_source_imported)
            EntrySource.RECORDED -> stringResource(R.string.details_source_recorded)
        }
        is DetailsMode.SharedPreview -> stringResource(R.string.details_shared_preview)
    }

    // Hoisted so dragging the elevation profile can mark the matching point on the hero route.
    var scrubFraction by remember { mutableStateOf<Float?>(null) }
    var trimRange by remember(entry.id) { mutableStateOf<ClosedFloatingPointRange<Float>?>(null) }
    var showFolderSheet by remember { mutableStateOf(false) }
    var showPhotoSheet by remember { mutableStateOf(false) }
    val viewMode = mode as? DetailsMode.View
    val trimCopyName = stringResource(R.string.details_trim_copy_name, entry.name)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            DetailsHeroMap(
                entry = entry,
                highlightFraction = scrubFraction,
                keptRange = trimRange,
                isSharingLink = params.uiState.isSharingLink,
                onNavigateBack = params.onNavigateBack,
                sourceLabel = sourceLabel,
                onToggleFavorite = (mode as? DetailsMode.View)?.onToggleFavorite,
                onRename = (mode as? DetailsMode.View)?.onRename,
                onTrim = if (viewMode != null && entry.fileDownloaded && entry.pointCount >= 2) {
                    { trimRange = 0f..1f }
                } else {
                    null
                },
                onMoveToFolder = viewMode?.let { { showFolderSheet = true } },
                onDelete = (mode as? DetailsMode.View)?.onDelete,
                onShareLink = (mode as? DetailsMode.View)?.onShareLink,
                onShareFile = (mode as? DetailsMode.View)?.onShareFile,
                onSaveFile = (mode as? DetailsMode.View)?.onSaveFile
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RyggTheme.dimens.commonContentPadding16),
                verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
            ) {
                if (mode is DetailsMode.SavePreview && mode.name != null && mode.onNameChange != null) {
                    RyggTextField(
                        value = mode.name,
                        onValueChange = mode.onNameChange,
                        labelText = stringResource(R.string.details_file_name),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                DetailsStatsGrid(entry)

                if (viewMode != null) {
                    Section(title = stringResource(R.string.details_photos)) {
                        DetailsGalleryCard(
                            images = entry.images,
                            isAdding = params.uiState.isAddingPhotos,
                            onAddPhotos = viewMode.onAddPhotos,
                            onOpenPhoto = viewMode.onOpenPhoto,
                            onManagePhotos = { showPhotoSheet = true }
                        )
                    }
                }

                val range = trimRange
                if (elevationProfile.isNotEmpty()) {
                    ElevationProfile(
                        samples = elevationProfile,
                        onScrubChange = { scrubFraction = it },
                        trimRange = range,
                        onTrimRangeChange = { trimRange = it }
                    )
                } else if (range != null) {
                    RangeSlider(
                        value = range,
                        onValueChange = { trimRange = it },
                        colors = SliderDefaults.colors(
                            thumbColor = RyggTheme.getColor(RyggColor.BrandGreen),
                            activeTrackColor = RyggTheme.getColor(RyggColor.BrandGreen),
                            inactiveTrackColor = RyggTheme.getColor(RyggColor.MossSurface)
                        )
                    )
                }
                if (range != null) {
                    Text(
                        text = stringResource(R.string.details_trim_hint),
                        style = RyggTheme.typography.bodySmall,
                        color = RyggTheme.getColor(RyggColor.TextSecondary)
                    )
                }

                Section(title = stringResource(R.string.details_contents)) {
                    DetailsContentsCard(entry)
                }

                if (entry.description.isNotBlank() || entry.folder != null) {
                    Section(title = stringResource(R.string.details_details)) {
                        DetailsMetaCard(entry)
                    }
                }
            }
        }

        val range = trimRange
        if (range != null && viewMode != null) {
            TrimActionBar(
                isSaving = params.uiState.isSavingTrim,
                onSave = { viewMode.onSaveTrim(range.start.toDouble(), range.endInclusive.toDouble(), trimCopyName) },
                onCancel = { trimRange = null }
            )
        } else {
            BottomActionBar(mode = mode, entryId = entry.id, onNavigateBack = params.onNavigateBack)
        }
    }

    if (showPhotoSheet && viewMode != null) {
        PhotoManageSheet(
            images = entry.images,
            onReorder = viewMode.onReorderPhotos,
            onDismiss = { showPhotoSheet = false }
        )
    }

    if (showFolderSheet && viewMode != null) {
        FolderPickerSheet(
            currentFolder = entry.folder,
            folders = params.uiState.folders,
            onFolderPicked = { folder ->
                showFolderSheet = false
                viewMode.onSetFolder(folder)
            },
            onDismiss = { showFolderSheet = false }
        )
    }
}

// Shaped like the hero and stat card that are coming, so nothing jumps when they land.
@Composable
private fun DetailsSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RyggTheme.dimens.detailsHeroHeight)
                .shimmer()
        )
        Column(
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing16)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RyggTheme.dimens.detailsSkeletonStats)
                    .clip(RyggShapes.card)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RyggTheme.dimens.elevationProfileHeight)
                    .clip(RyggShapes.card)
                    .shimmer()
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)) {
        DetailsSectionTitle(text = title)
        content()
    }
}

@Composable
private fun TrimActionBar(
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        color = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shadowElevation = RyggTheme.dimens.elevation4,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            RyggPrimaryButton(
                text = stringResource(R.string.details_trim_save),
                onClick = onSave,
                isLoading = isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            RyggSecondaryButton(
                text = stringResource(R.string.details_rename_cancel),
                onClick = onCancel,
                isEnabled = !isSaving,
                textColor = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    mode: DetailsMode,
    entryId: Long,
    onNavigateBack: () -> Unit
) {
    Surface(
        color = RyggTheme.getColor(RyggColor.SurfaceElevated),
        shadowElevation = RyggTheme.dimens.elevation4,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            when (mode) {
                is DetailsMode.View ->
                    RyggPrimaryButton(
                        text = stringResource(R.string.details_view_on_map),
                        onClick = { mode.onViewOnMap(entryId) },
                        modifier = Modifier.fillMaxWidth()
                    )

                is DetailsMode.SavePreview -> {
                    RyggPrimaryButton(
                        text = stringResource(R.string.details_save_to_library),
                        onClick = mode.onSave,
                        modifier = Modifier.fillMaxWidth()
                    )
                    RyggSecondaryButton(
                        text = stringResource(R.string.details_discard),
                        onClick = onNavigateBack,
                        textColor = RyggTheme.getColor(RyggColor.TextSecondary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is DetailsMode.SharedPreview ->
                    if (mode.isInLibrary) {
                        Text(
                            text = stringResource(R.string.details_shared_in_library),
                            style = RyggTheme.typography.bodyMedium,
                            color = RyggTheme.getColor(RyggColor.TextSecondary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        RyggPrimaryButton(
                            text = stringResource(R.string.details_open_in_library),
                            onClick = mode.onOpenInLibrary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        RyggPrimaryButton(
                            text = stringResource(R.string.details_save_to_library),
                            onClick = mode.onSaveCopy,
                            isLoading = mode.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScreenPreview(
    @PreviewParameter(DetailsParamsProvider::class) params: DetailsScreenParams
) {
    RyggTheme {
        DetailsScreen(params = params)
    }
}

data class DetailsScreenParams(
    val uiState: DetailsUiState,
    val onNavigateBack: () -> Unit,
    val mode: DetailsMode
)

sealed interface DetailsMode {
    data class View(
        val onViewOnMap: (Long) -> Unit,
        val onToggleFavorite: () -> Unit,
        val onRename: (String) -> Unit,
        val onSetFolder: (String?) -> Unit,
        val onSaveTrim: (Double, Double, String) -> Unit,
        val onDelete: () -> Unit,
        val onShareLink: () -> Unit,
        val onShareFile: () -> Unit,
        val onSaveFile: () -> Unit,
        val onAddPhotos: () -> Unit,
        val onOpenPhoto: (String) -> Unit,
        val onReorderPhotos: (List<String>) -> Unit
    ) : DetailsMode

    // Preview of a not-yet-saved entry (import or recording). When [name] is non-null an
    // editable file-name field is shown and its value is used on save.
    data class SavePreview(
        val name: String?,
        val onNameChange: ((String) -> Unit)?,
        val onSave: () -> Unit
    ) : DetailsMode

    // A shared route opened from a link: save a copy, or open the copy already in the library.
    data class SharedPreview(
        val isSaving: Boolean,
        val isInLibrary: Boolean,
        val onSaveCopy: () -> Unit,
        val onOpenInLibrary: () -> Unit
    ) : DetailsMode
}
