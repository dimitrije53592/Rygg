package com.example.rygg.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.rygg.core.ui.theme.RyggColor
import com.example.rygg.core.ui.theme.RyggTheme
import com.example.rygg.core.units.Quantity

enum class RyggStatSize { Small, Medium, Large, Hero }

// The one way a number is shown in the app: figure, a smaller unit on its baseline, and a tracked
// label beneath. Read by TalkBack as a single "label, value unit" phrase.
@Composable
fun RyggStat(
    quantity: Quantity,
    label: String?,
    modifier: Modifier = Modifier,
    size: RyggStatSize = RyggStatSize.Medium,
    valueColor: Color = RyggTheme.getColor(RyggColor.TextPrimary),
    secondaryColor: Color = RyggTheme.getColor(RyggColor.TextSecondary),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    val valueStyle = statValueStyle(size)
    val spoken = listOfNotNull(label, listOfNotNull(quantity.value, quantity.unit).joinToString(" "))
        .joinToString(", ")

    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = spoken },
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing4)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = quantity.value,
                style = valueStyle,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alignByBaseline()
            )
            quantity.unit?.let { unit ->
                Text(
                    text = unit,
                    style = unitStyle(valueStyle),
                    color = secondaryColor,
                    maxLines = 1,
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(start = RyggTheme.dimens.commonSpacing2)
                )
            }
        }
        label?.let {
            Text(
                text = it.uppercase(),
                style = RyggTheme.textStyles.trackedLabel,
                color = secondaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RyggStat(
    value: String,
    label: String?,
    modifier: Modifier = Modifier,
    size: RyggStatSize = RyggStatSize.Medium,
    valueColor: Color = RyggTheme.getColor(RyggColor.TextPrimary),
    secondaryColor: Color = RyggTheme.getColor(RyggColor.TextSecondary),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    RyggStat(
        quantity = Quantity(value),
        label = label,
        modifier = modifier,
        size = size,
        valueColor = valueColor,
        secondaryColor = secondaryColor,
        horizontalAlignment = horizontalAlignment
    )
}

@Composable
private fun statValueStyle(size: RyggStatSize): TextStyle = when (size) {
    RyggStatSize.Small -> RyggTheme.textStyles.statValueSmall
    RyggStatSize.Medium -> RyggTheme.textStyles.statValue
    RyggStatSize.Large -> RyggTheme.textStyles.statValueLarge
    RyggStatSize.Hero -> RyggTheme.textStyles.statValueHero
}

// Units read as annotation, not data: ~55% of the figure, never below legible label size.
private fun unitStyle(valueStyle: TextStyle): TextStyle = valueStyle.copy(
    fontSize = (valueStyle.fontSize.value * UNIT_SCALE).coerceAtLeast(MIN_UNIT_SP).sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp
)

@Preview(showBackground = true)
@Composable
private fun RyggStatPreview() {
    RyggTheme {
        Row(
            modifier = Modifier.padding(RyggTheme.dimens.commonContentPadding16),
            horizontalArrangement = Arrangement.spacedBy(RyggTheme.dimens.commonSpacing24)
        ) {
            RyggStat(quantity = Quantity("12.4", "km"), label = "Distance", size = RyggStatSize.Large)
            RyggStat(quantity = Quantity("1,240", "m"), label = "Ascent")
            RyggStat(value = "3h 42m", label = "Moving time", size = RyggStatSize.Small)
        }
    }
}

private const val UNIT_SCALE = 0.55f
private const val MIN_UNIT_SP = 11f
