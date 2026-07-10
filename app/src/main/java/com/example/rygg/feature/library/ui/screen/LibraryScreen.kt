package com.example.rygg.feature.library.ui.screen

import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggTopAppBar
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.core.ui.utils.rememberFilePicker
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.ui.viewmodel.LibraryUiState

@Composable
fun LibraryScreen(params: LibraryScreenParams) {
    val launchFilePicker = rememberFilePicker(onFilePicked = params.onFilePicked)

    Scaffold(
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.library_title),
                actions = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RyggTheme.getColor(RyggColor.SurfaceDim))
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
        ) {
            Log.d("Sofija", params.uiState.gpxFileEntries.toString())
            LibraryToolbar(Discipline.entries)
            OutlinedButton(
                onClick = { launchFilePicker() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.home_send_notification))
            }
        }
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
                    gpxFileEntries = emptyList(),
                    isLoading = false
                ),
                onFilePicked = {}
            )
        )
    }
}

data class LibraryScreenParams(
    val uiState: LibraryUiState,
    val onFilePicked: (Uri) -> Unit
)
