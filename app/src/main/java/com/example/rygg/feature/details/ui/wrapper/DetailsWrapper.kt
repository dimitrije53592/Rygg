package com.example.rygg.feature.details.ui.wrapper

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.R
import com.example.rygg.core.ui.utils.shareGpxFile
import com.example.rygg.core.ui.utils.shareRouteLink
import com.example.rygg.feature.details.ui.screen.DetailsMode
import com.example.rygg.feature.details.ui.screen.DetailsScreen
import com.example.rygg.feature.details.ui.screen.DetailsScreenParams
import com.example.rygg.feature.details.ui.viewmodel.DetailsEvent
import com.example.rygg.feature.details.ui.viewmodel.DetailsViewModel

@Composable
fun DetailsWrapper(
    onNavigateBack: () -> Unit,
    onViewOnMap: (Long) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareFailedMessage = stringResource(R.string.details_share_failed)
    val fileNotReadyMessage = stringResource(R.string.details_share_file_not_ready)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailsEvent.ShareLink -> context.shareRouteLink(event.url, event.routeName)
                is DetailsEvent.ShareFile -> context.shareGpxFile(event.uri, event.routeName)
                DetailsEvent.ShareLinkFailed ->
                    Toast.makeText(context, shareFailedMessage, Toast.LENGTH_SHORT).show()

                DetailsEvent.FileNotReady ->
                    Toast.makeText(context, fileNotReadyMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    DetailsScreen(
        params = DetailsScreenParams(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            mode = DetailsMode.View(
                onViewOnMap = onViewOnMap,
                onToggleFavorite = { viewModel.onToggleFavorite() },
                onRename = { viewModel.onRename(it) },
                onDelete = {
                    viewModel.onDelete()
                    onNavigateBack()
                },
                onShareLink = { viewModel.onShareLink() },
                onShareFile = { viewModel.onShareFile() }
            )
        )
    )
}
