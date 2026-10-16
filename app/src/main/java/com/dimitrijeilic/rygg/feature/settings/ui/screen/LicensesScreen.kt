package com.dimitrijeilic.rygg.feature.settings.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.LoadingIndicator
import com.dimitrijeilic.rygg.core.ui.components.RyggCard
import com.dimitrijeilic.rygg.core.ui.components.RyggTopAppBar
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.feature.settings.domain.LegalLinks
import com.dimitrijeilic.rygg.feature.settings.domain.OpenSourceLibrary
import com.dimitrijeilic.rygg.feature.settings.domain.OpenSourceLicenses
import com.dimitrijeilic.rygg.feature.settings.ui.viewmodel.LicensesLoadingState
import com.dimitrijeilic.rygg.feature.settings.ui.viewmodel.LicensesUiState

private const val FONT_ENTRY_ID = "font:manrope"

@Composable
fun LicensesScreen(params: LicensesScreenParams) {
    Scaffold(
        containerColor = RyggTheme.getColor(RyggColor.SurfaceDim),
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.licenses_title),
                onNavigateBack = params.onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = params.uiState.loadingState) {
                LicensesLoadingState.Loading ->
                    LoadingIndicator(text = stringResource(R.string.licenses_loading))

                is LicensesLoadingState.Error -> Text(
                    text = stringResource(R.string.licenses_error),
                    style = RyggTheme.typography.bodyMedium,
                    color = RyggTheme.getColor(RyggColor.TextSecondary),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding32)
                )

                is LicensesLoadingState.Loaded -> LoadedContent(
                    licenses = state.licenses,
                    params = params
                )
            }
        }
    }
}

// Map data and font come first: they carry attribution obligations, the libraries are the long tail.
@Composable
private fun LoadedContent(licenses: OpenSourceLicenses, params: LicensesScreenParams) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(RyggTheme.dimens.commonContentPadding16),
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        item { SectionHeader(text = stringResource(R.string.licenses_section_map_data)) }
        item {
            LicenseRow(
                title = stringResource(R.string.licenses_osm_title),
                detail = stringResource(R.string.licenses_osm_detail),
                onClick = { params.onOpenUrl(LegalLinks.OPENSTREETMAP_COPYRIGHT) },
                trailing = LicenseRowTrailing.External
            )
        }
        item {
            LicenseRow(
                title = stringResource(R.string.licenses_maptiler_title),
                detail = stringResource(R.string.licenses_maptiler_detail),
                onClick = { params.onOpenUrl(LegalLinks.MAPTILER_COPYRIGHT) },
                trailing = LicenseRowTrailing.External
            )
        }

        item { SectionHeader(text = stringResource(R.string.licenses_section_fonts)) }
        item {
            val expanded = params.uiState.expandedId == FONT_ENTRY_ID
            LicenseRow(
                title = stringResource(R.string.licenses_manrope_title),
                detail = stringResource(R.string.licenses_manrope_detail),
                onClick = { params.onToggleExpanded(FONT_ENTRY_ID) },
                trailing = if (expanded) LicenseRowTrailing.Collapse else LicenseRowTrailing.Expand,
                expandedText = licenses.fontLicenseText.takeIf { expanded }
            )
        }

        item {
            SectionHeader(
                text = stringResource(R.string.licenses_section_libraries, licenses.libraries.size)
            )
        }
        items(items = licenses.libraries, key = { it.id }) { library ->
            LibraryRow(library = library, params = params)
        }
    }
}

// Rows with bundled licence text expand in place; the rest link out to the project page.
@Composable
private fun LibraryRow(library: OpenSourceLibrary, params: LicensesScreenParams) {
    val expanded = params.uiState.expandedId == library.id
    val detail = listOfNotNull(library.version, library.licenseNames.joinToString().ifEmpty { null })
        .joinToString(separator = " · ")
    when {
        library.licenseText != null -> LicenseRow(
            title = library.name,
            detail = detail,
            onClick = { params.onToggleExpanded(library.id) },
            trailing = if (expanded) LicenseRowTrailing.Collapse else LicenseRowTrailing.Expand,
            expandedText = library.licenseText.takeIf { expanded }
        )

        library.url != null -> LicenseRow(
            title = library.name,
            detail = detail,
            onClick = { params.onOpenUrl(library.url) },
            trailing = LicenseRowTrailing.External
        )

        else -> LicenseRow(title = library.name, detail = detail)
    }
}

private enum class LicenseRowTrailing { Expand, Collapse, External }

@Composable
private fun LicenseRow(
    title: String,
    detail: String,
    onClick: (() -> Unit)? = null,
    trailing: LicenseRowTrailing? = null,
    expandedText: String? = null
) {
    RyggCard(contentPadding = RyggTheme.dimens.zeroPadding) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(RyggTheme.dimens.commonContentPadding16),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = RyggTheme.typography.bodyLarge,
                        color = RyggTheme.getColor(RyggColor.TextPrimary)
                    )
                    if (detail.isNotEmpty()) {
                        Text(
                            text = detail,
                            style = RyggTheme.typography.bodySmall,
                            color = RyggTheme.getColor(RyggColor.TextSecondary)
                        )
                    }
                }
                trailing?.let {
                    Icon(
                        imageVector = when (it) {
                            LicenseRowTrailing.Expand -> Icons.Outlined.ExpandMore
                            LicenseRowTrailing.Collapse -> Icons.Outlined.ExpandLess
                            LicenseRowTrailing.External -> Icons.AutoMirrored.Outlined.OpenInNew
                        },
                        contentDescription = null,
                        tint = RyggTheme.getColor(RyggColor.TextSecondary),
                        modifier = Modifier.size(RyggTheme.dimens.iconSize24)
                    )
                }
            }
            expandedText?.let {
                Text(
                    text = it,
                    style = RyggTheme.typography.bodySmall,
                    color = RyggTheme.getColor(RyggColor.TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = RyggTheme.textStyles.trackedLabel,
        color = RyggTheme.getColor(RyggColor.TextSecondary),
        modifier = Modifier.padding(
            start = RyggTheme.dimens.commonContentPadding4,
            top = RyggTheme.dimens.commonContentPadding16
        )
    )
}

@Preview
@Composable
private fun LicensesScreenPreview() {
    RyggTheme {
        LicensesScreen(
            params = LicensesScreenParams(
                uiState = LicensesUiState(
                    loadingState = LicensesLoadingState.Loaded(
                        OpenSourceLicenses(
                            fontLicenseText = "SIL Open Font License, Version 1.1",
                            libraries = listOf(
                                OpenSourceLibrary(
                                    id = "androidx.core:core-ktx",
                                    name = "Core Kotlin Extensions",
                                    version = "1.19.0",
                                    licenseNames = listOf("Apache License 2.0"),
                                    licenseText = "Apache License, Version 2.0",
                                    url = null
                                ),
                                OpenSourceLibrary(
                                    id = "org.maplibre.compose:maplibre-compose",
                                    name = "MapLibre Compose",
                                    version = "0.13.1",
                                    licenseNames = listOf("BSD-3-Clause"),
                                    licenseText = null,
                                    url = "https://github.com/maplibre/maplibre-compose"
                                )
                            )
                        )
                    ),
                    expandedId = "androidx.core:core-ktx"
                ),
                onNavigateBack = {},
                onToggleExpanded = {},
                onOpenUrl = {}
            )
        )
    }
}

data class LicensesScreenParams(
    val uiState: LicensesUiState,
    val onNavigateBack: () -> Unit,
    val onToggleExpanded: (String) -> Unit,
    val onOpenUrl: (String) -> Unit
)
