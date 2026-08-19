package com.example.rygg.feature.library.ui.paramproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.rygg.feature.auth.domain.Discipline
import com.example.rygg.feature.library.domain.EntrySource
import com.example.rygg.feature.library.domain.GpxFileEntry

// Favorite/recorded vs plain/imported — exercises the star, badge and date-vs-imported subtitle.
class GpxFileEntryProvider : PreviewParameterProvider<GpxFileEntry> {
    override val values = sequenceOf(
        previewGpxFileEntry(id = 1L, name = "Mali i Veliki Vukan"),
        previewGpxFileEntry(
            id = 2L,
            name = "Seven Lakes valley",
            discipline = Discipline.RIDE,
            source = EntrySource.IMPORTED,
            isFavorite = false,
            hasTime = false,
            tags = emptyList()
        )
    )
}
