package com.dimitrijeilic.rygg.feature.details.ui.wrapper

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.utils.GPX_MIME_TYPE
import com.dimitrijeilic.rygg.core.ui.utils.rememberDocumentCreator
import com.dimitrijeilic.rygg.core.ui.utils.shareGpxFile
import com.dimitrijeilic.rygg.core.ui.utils.shareRouteLink
import com.dimitrijeilic.rygg.feature.details.ui.screen.DetailsMode
import com.dimitrijeilic.rygg.feature.details.ui.screen.DetailsScreen
import com.dimitrijeilic.rygg.feature.details.ui.screen.DetailsScreenParams
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.DetailsEvent
import com.dimitrijeilic.rygg.feature.details.ui.viewmodel.DetailsViewModel

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
    val fileSavedMessage = stringResource(R.string.details_save_file_done)
    val fileSaveFailedMessage = stringResource(R.string.details_save_file_failed)
    val launchSaveAs = rememberDocumentCreator(
        mimeType = GPX_MIME_TYPE,
        onCreated = viewModel::onSaveFileTo
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailsEvent.ShareLink -> context.shareRouteLink(event.url, event.routeName)
                is DetailsEvent.ShareFile -> context.shareGpxFile(event.uri, event.routeName)
                DetailsEvent.ShareLinkFailed ->
                    Toast.makeText(context, shareFailedMessage, Toast.LENGTH_SHORT).show()

                DetailsEvent.FileNotReady ->
                    Toast.makeText(context, fileNotReadyMessage, Toast.LENGTH_SHORT).show()

                is DetailsEvent.PickSaveTarget -> launchSaveAs(event.suggestedName)

                DetailsEvent.FileSaved ->
                    Toast.makeText(context, fileSavedMessage, Toast.LENGTH_SHORT).show()

                DetailsEvent.FileSaveFailed ->
                    Toast.makeText(context, fileSaveFailedMessage, Toast.LENGTH_SHORT).show()
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
                onShareFile = { viewModel.onShareFile() },
                onSaveFile = { viewModel.onSaveFileRequested() }
            )
        )
    )
}
