package com.example.rygg.feature.details.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.rygg.R
import com.example.rygg.core.gpx.model.ElevationSample
import com.example.rygg.core.ui.components.LoadingIndicator
import com.example.rygg.core.ui.components.RyggPrimaryButton
import com.example.rygg.core.ui.components.RyggTextField
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.feature.details.ui.components.DetailsContentsCard
import com.example.rygg.feature.details.ui.components.DetailsHeroMap
import com.example.rygg.feature.details.ui.components.DetailsMetaCard
import com.example.rygg.feature.details.ui.components.DetailsSectionTitle
import com.example.rygg.feature.details.ui.components.DetailsStatsGrid
import com.example.rygg.feature.details.ui.components.ElevationProfile
import com.example.rygg.feature.details.ui.paramproviders.DetailsParamsProvider
import com.example.rygg.feature.details.ui.viewmodel.DetailsLoadingState
import com.example.rygg.feature.details.ui.viewmodel.DetailsUiState
import com.example.rygg.feature.library.domain.EntrySource
import com.example.rygg.feature.library.domain.GpxFileEntry

@Composable
fun DetailsScreen(params: DetailsScreenParams) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RyggTheme.getColor(RyggColor.SurfaceDim))
    ) {
        when (val state = params.uiState.loadingState) {
            is DetailsLoadingState.Loading ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator(text = stringResource(R.string.follow_locating))
                }

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
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            DetailsHeroMap(
                entry = entry,
                onNavigateBack = params.onNavigateBack,
                sourceLabel = sourceLabel,
                onToggleFavorite = (mode as? DetailsMode.View)?.onToggleFavorite,
                onDelete = (mode as? DetailsMode.View)?.onDelete
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

                if (elevationProfile.isNotEmpty()) {
                    ElevationProfile(samples = elevationProfile)
                }

                Section(title = stringResource(R.string.details_contents)) {
                    DetailsContentsCard(entry)
                }

                if (entry.description.isNotBlank() || entry.tags.isNotEmpty()) {
                    Section(title = stringResource(R.string.details_details)) {
                        DetailsMetaCard(entry)
                    }
                }
            }
        }

        BottomActionBar(mode = mode, entryId = entry.id, onNavigateBack = params.onNavigateBack)
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
                    TextButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.details_discard),
                            style = RyggTheme.typography.titleMedium,
                            color = RyggTheme.getColor(RyggColor.BrandGreen)
                        )
                    }
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
        val onDelete: () -> Unit
    ) : DetailsMode

    // Preview of a not-yet-saved entry (import or recording). When [name] is non-null an
    // editable file-name field is shown and its value is used on save.
    data class SavePreview(
        val name: String?,
        val onNameChange: ((String) -> Unit)?,
        val onSave: () -> Unit
    ) : DetailsMode
}
