package com.example.rygg.feature.home.ui.wrapper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.R
import com.example.rygg.feature.home.ui.screen.HomeScreen
import com.example.rygg.feature.home.ui.screen.HomeScreenParams
import com.example.rygg.feature.home.ui.viewmodel.HomeViewModel

@Composable
fun HomeWrapper(
    onItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val title = stringResource(R.string.notification_demo_title)
    val message = stringResource(R.string.notification_demo_text)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.sendNotification(title, message)
    }

    HomeScreen(
        params = HomeScreenParams(
            uiState = uiState,
            onAddItem = viewModel::addItem,
            onDeleteItem = viewModel::deleteItem,
            onItemClick = onItemClick,
            onSendNotification = {
                val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                if (needsPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.sendNotification(title, message)
                }
            }
        )
    )
}
