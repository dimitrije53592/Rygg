package com.example.rygg.feature.library.ui.wrapper

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rygg.feature.library.ui.screen.LibraryScreen
import com.example.rygg.feature.library.ui.screen.LibraryScreenParams
import com.example.rygg.feature.library.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryWrapper(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreen(
        params = LibraryScreenParams(
            uiState = uiState,
            onFilePicked = { uri ->
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val xml = inputStream.bufferedReader().use { it.readText() }
                    xml.chunked(3500).forEachIndexed { i, chunk ->
                        Log.d("Sofija", "part $i:\n$chunk")
                    }

//                    GpxParser.parse(inputStream)
                }
            }
        )
    )
}
