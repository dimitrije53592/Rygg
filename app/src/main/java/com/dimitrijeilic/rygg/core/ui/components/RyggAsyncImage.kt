package com.dimitrijeilic.rygg.core.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

// A route photo with a shimmer while decoding and a placeholder while it waits on sync.
@Composable
fun RyggAsyncImage(
    uri: Uri,
    isAvailable: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showStatusLabel: Boolean = true
) {
    if (!isAvailable) {
        ImagePlaceholder(
            icon = Icons.Outlined.CloudDownload,
            label = stringResource(R.string.photo_waiting_for_sync).takeIf { showStatusLabel },
            modifier = modifier
        )
        return
    }
    var state by remember(uri) { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    Box(modifier = modifier) {
        AsyncImage(
            model = uri,
            contentDescription = contentDescription,
            contentScale = contentScale,
            onState = { state = it },
            modifier = Modifier
                .fillMaxSize()
                .then(if (state is AsyncImagePainter.State.Loading) Modifier.shimmer() else Modifier)
        )
        if (state is AsyncImagePainter.State.Error) {
            ImagePlaceholder(
                icon = Icons.Outlined.BrokenImage,
                label = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ImagePlaceholder(
    icon: ImageVector,
    label: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(RyggTheme.getColor(RyggColor.Surface)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RyggTheme.getColor(RyggColor.TextSecondary),
                modifier = Modifier.size(RyggTheme.dimens.iconSize32)
            )
            if (label != null) {
                Text(
                    text = label,
                    style = RyggTheme.typography.bodySmall,
                    color = RyggTheme.getColor(RyggColor.TextSecondary),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 240, heightDp = 160)
@Composable
private fun RyggAsyncImageWaitingPreview() {
    RyggTheme {
        RyggAsyncImage(
            uri = Uri.EMPTY,
            isAvailable = false,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
