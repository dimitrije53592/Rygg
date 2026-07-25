package com.example.rygg.feature.map.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.components.RyggTopAppBar
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

@Composable
fun MapScreen(params: MapScreenParams) {
    Scaffold(
        topBar = {
            RyggTopAppBar(
                title = stringResource(R.string.nav_map),
                actions = {}
            )
        }
    ) { innerPadding ->
        MaplibreMap(
            baseStyle = BaseStyle.Uri(params.styleUrl),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

data class MapScreenParams(
    val styleUrl: String
)
