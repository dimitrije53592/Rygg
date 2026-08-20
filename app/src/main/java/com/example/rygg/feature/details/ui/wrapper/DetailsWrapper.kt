package com.example.rygg.feature.details.ui.wrapper

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.R
import com.example.rygg.core.common.Outcome
import com.example.rygg.core.ui.utils.shareGpxFile
import com.example.rygg.core.ui.utils.shareRouteLink
import com.example.rygg.feature.details.ui.screen.DetailsMode
import com.example.rygg.feature.details.ui.screen.DetailsScreen
import com.example.rygg.feature.details.ui.screen.DetailsScreenParams
import com.example.rygg.feature.details.ui.viewmodel.DetailsLoadingState
import com.example.rygg.feature.details.ui.viewmodel.DetailsViewModel
import kotlinx.coroutines.launch

@Composable
fun DetailsWrapper(
    onNavigateBack: () -> Unit,
    onViewOnMap: (Long) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val routeName = (uiState.loadingState as? DetailsLoadingState.Loaded)?.entry?.name.orEmpty()
    val shareFailedMessage = stringResource(R.string.details_share_failed)

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
                onShareLink = {
                    scope.launch {
                        when (val outcome = viewModel.createShareLink()) {
                            is Outcome.Success -> context.shareRouteLink(outcome.data, routeName)
                            is Outcome.Error ->
                                Toast.makeText(context, shareFailedMessage, Toast.LENGTH_SHORT).show()
                            Outcome.Loading -> Unit
                        }
                    }
                },
                onShareFile = {
                    viewModel.shareFileUri()?.let { context.shareGpxFile(it, routeName) }
                }
            )
        )
    )
}
