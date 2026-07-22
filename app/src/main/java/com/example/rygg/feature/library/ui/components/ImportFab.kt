package com.example.rygg.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.rygg.R
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.ui.utils.capitalize
import com.example.rygg.feature.auth.domain.Discipline

@Composable
internal fun ImportFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onDisciplinePicked: (Discipline) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing12)
    ) {
        if (expanded) {
            Discipline.entries.forEach { discipline ->
                DisciplineFabOption(
                    discipline = discipline,
                    onClick = { onDisciplinePicked(discipline) }
                )
            }
        }
        FloatingActionButton(
            onClick = onToggle,
            containerColor = RyggTheme.getColor(RyggColor.BrandGreen),
            contentColor = RyggTheme.getColor(RyggColor.OnBrand)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = stringResource(R.string.library_import)
            )
        }
    }
}

@Composable
private fun DisciplineFabOption(
    discipline: Discipline,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing8)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(RyggTheme.dimens.radius8))
                .background(RyggTheme.getColor(RyggColor.SurfaceElevated))
                .padding(
                    horizontal = RyggTheme.dimens.commonContentPadding12,
                    vertical = RyggTheme.dimens.commonContentPadding4
                )
        ) {
            Text(
                text = discipline.name.capitalize(),
                style = RyggTheme.typography.labelMedium,
                color = RyggTheme.getColor(RyggColor.TextPrimary)
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = RyggTheme.getColor(RyggColor.SurfaceElevated),
            contentColor = RyggTheme.getColor(RyggColor.BrandGreen)
        ) {
            Icon(
                painter = painterResource(discipline.iconRes),
                contentDescription = discipline.name,
                modifier = Modifier.size(RyggTheme.dimens.iconSize24)
            )
        }
    }
}
