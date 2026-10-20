package com.dimitrijeilic.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.ui.components.RyggTextField
import com.dimitrijeilic.rygg.core.ui.components.RyggTopBarAction
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme

@Composable
internal fun LibrarySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RyggTheme.getColor(RyggColor.SurfaceDim))
            .padding(
                start = RyggTheme.dimens.commonContentPadding16,
                end = RyggTheme.dimens.commonContentPadding8,
                top = RyggTheme.dimens.commonContentPadding8
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            RyggTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholderText = stringResource(R.string.library_search_hint),
                leadingIcon = painterResource(R.drawable.ic_search),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
        RyggTopBarAction(
            icon = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.library_search_close),
            onClick = onClose
        )
    }
}

@Preview
@Composable
private fun LibrarySearchBarPreview() {
    RyggTheme {
        LibrarySearchBar(query = "Triglav", onQueryChange = {}, onClose = {})
    }
}
