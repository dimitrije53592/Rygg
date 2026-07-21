package com.example.rygg.feature.library.ui.screen

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.core.ui.utils.rememberFilePicker
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.domain.GpxFileEntry
import com.example.rygg.feature.library.ui.components.GpxFileEntryCard
import com.example.rygg.feature.library.ui.viewmodel.GpxFilesLoadingState
import com.example.rygg.feature.library.ui.viewmodel.LibraryUiState

@Composable
fun LibraryScreen(params: LibraryScreenParams) {
    var fabExpanded by remember { mutableStateOf(false) }
    var pendingDiscipline by remember { mutableStateOf(Discipline.HIKE) }
    val launchFilePicker = rememberFilePicker(
        onFilePicked = { uri -> params.onImport(uri, pendingDiscipline) }
    )

    Scaffold(
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.library_title),
                actions = {}
            )
        },
        floatingActionButton = {
            ImportFab(
                expanded = fabExpanded,
                onToggle = { fabExpanded = !fabExpanded },
                onDisciplinePicked = { discipline ->
                    pendingDiscipline = discipline
                    fabExpanded = false
                    launchFilePicker()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RyggTheme.getColor(RyggColor.SurfaceDim))
                .padding(innerPadding)
        ) {
            LibraryToolbar(Discipline.entries)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (params.uiState.gpxFilesLoadingState) {
                    is GpxFilesLoadingState.Loading -> LoadingState()
                    is GpxFilesLoadingState.GpxFilesLoaded -> {
                        val entries = params.uiState.gpxFilesLoadingState.gpxFilesEntries
                        if (entries.isEmpty()) {
                            EmptyState()
                        } else {
                            EntryList(
                                entries = entries,
                                onEntryClick = params.onEntryClick,
                                onFavoriteClick = params.onFavoriteClick
                            )
                        }
                    }

                    is GpxFilesLoadingState.Error ->
                        ErrorState(errorMessage = params.uiState.gpxFilesLoadingState.errorMessage)
                }
            }
        }
    }
}

@Composable
private fun ImportFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onDisciplinePicked: (Discipline) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        if (expanded) {
            Discipline.entries.forEach { discipline ->
                DisciplineFabOption(
                    discipline = discipline,
                    onClick = { onDisciplinePicked(discipline) }
                )
            }
        }
        FloatingActionButton(
            onClick = onToggle,
            containerColor = RyggTheme.getColor(RyggColor.BrandGreen),
            contentColor = RyggTheme.getColor(RyggColor.OnBrand)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = stringResource(R.string.library_import)
            )
        }
    }
}

@Composable
private fun DisciplineFabOption(
    discipline: Discipline,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
                .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
                .padding(
                    horizontal = RyggTheme.dimens.commonContentPadding12,
                    vertical = RyggTheme.dimens.commonContentPadding4
                )
        ) {
            Text(
                text = discipline.name.capitalize(),
                style = RyggTheme.typography.labelMedium,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
            contentColor = RyggTheme.getColor(RyggColor.BrandGreen)
        ) {
            Icon(
                painter = painterResource(discipline.iconRes),
                contentDescription = discipline.name,
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
    }
}

@Composable
private fun EntryList(
    entries: List<GpxFileEntry>,
    onEntryClick: (GpxFileEntry) -> Unit,
    onFavoriteClick: (GpxFileEntry) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = pluralStringResource(R.plurals.library_activities, entries.size, entries.size),
            style = RyggTheme.typography.labelMedium,
            color = RyggTheme.getColor(RyggColor.TextSecondary),
            modifier = Modifier.padding(
                horizontal = RyggTheme.dimens.commonContentPadding20,
                vertical = RyggTheme.dimens.commonContentPadding8
            )
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = RyggTheme.dimens.commonContentPadding16,
                end = RyggTheme.dimens.commonContentPadding16,
                top = RyggTheme.dimens.commonContentPadding4,
                bottom = RyggTheme.dimens.commonContentPadding80
            ),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            items(entries, key = { it.id }) { entry ->
                GpxFileEntryCard(
                    entry = entry,
                    onClick = onEntryClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.library_empty_title),
                style = RyggTheme.typography.titleMedium,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
            Text(
                text = stringResource(R.string.library_empty_subtitle),
                style = RyggTheme.typography.bodyMedium,
                color = RyggTheme.getColor(RyggColor.TextSecondary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(
    errorMessage: String?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = RyggTheme.dimens.commonContentPadding16)
        ) {
            Text(
                text = errorMessage.orEmpty(),
                style = RyggTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = RyggTheme.getColor(RyggColor.BrandGreen))
    }
}

@Composable
private fun LibraryToolbar(
    disciplines: List<Discipline>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RyggTheme.getColor(RyggColor.BrandGraphite))
            .padding(vertical = RyggTheme.dimens.commonContentPadding12)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(
            space = RyggTheme.dimens.commonSpacing4,
            alignment = Alignment.Start
        )
    ) {
        DisciplineFilter(
            title = stringResource(R.string.library_filter_all),
            onClick = {}
        )
        disciplines.forEach {
            DisciplineFilter(
                title = it.name,
                iconRes = it.iconRes,
                onClick = {}
            )
        }
    }
}

@Composable
private fun DisciplineFilter(
    title: String,
    @DrawableRes iconRes: Int? = null,
    onClick: () -> Unit
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = RyggTheme.getColor(RyggColor.TextSecondary).copy(alpha = 0.7f)
        ),
        modifier = Modifier.height(RyggTheme.dimens.buttonSize32),
        onClick = onClick
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(iconRes),
                tint = RyggTheme.getColor(RyggColor.SurfaceElevated),
                contentDescription = "",
                modifier = Modifier.size(RyggTheme.dimens.iconSize16)
            )
            Spacer(Modifier.size(RyggTheme.dimens.commonSpacing4))
        }
        Text(
            text = title.capitalize(),
            color = RyggTheme.getColor(RyggColor.SurfaceElevated),
            style = RyggTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    RyggTheme {
        LibraryScreen(
            params = LibraryScreenParams(
                uiState = LibraryUiState(
                    gpxFilesLoadingState = GpxFilesLoadingState.GpxFilesLoaded(emptyList())
                ),
                onImport = { _, _ -> },
                onEntryClick = {},
                onFavoriteClick = {}
            )
        )
    }
}

data class LibraryScreenParams(
    val uiState: LibraryUiState,
    val onImport: (Uri, Discipline) -> Unit,
    val onEntryClick: (GpxFileEntry) -> Unit,
    val onFavoriteClick: (GpxFileEntry) -> Unit
)
