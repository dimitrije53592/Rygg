package com.dimitrijeilic.rygg.feature.offline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dimitrijeilic.rygg.core.ui.theme.RyggColor
import com.dimitrijeilic.rygg.core.ui.theme.RyggTheme
import com.dimitrijeilic.rygg.feature.map.ui.util.MapLabelFont
import com.dimitrijeilic.rygg.feature.offline.domain.OfflineRegion
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

@Composable
@MaplibreComposable
internal fun OfflineRegionLayers(regions: List<OfflineRegion>) {
    val data = remember(regions) { GeoJsonData.Features(regionFeatures(regions)) }
    val source = rememberGeoJsonSource(data)
    val green = RyggTheme.getColor(RyggColor.BrandGreen)

    FillLayer(
        id = "offline-regions-fill",
        source = source,
        color = const(green),
        opacity = const(FILL_OPACITY)
    )
    LineLayer(
        id = "offline-regions-outline",
        source = source,
        color = const(green),
        width = const(RyggTheme.dimens.border2)
    )
    SymbolLayer(
        id = "offline-regions-label",
        source = source,
        textField = format(span(feature.get(PROP_NAME).asString())),
        textFont = MapLabelFont,
        textColor = const(green),
        textHaloColor = const(RyggTheme.getColor(RyggColor.SurfaceElevated)),
        textHaloWidth = const(RyggTheme.dimens.border1)
    )
}

private fun regionFeatures(regions: List<OfflineRegion>): FeatureCollection<*, *> =
    FeatureCollection(
        regions.map { region ->
            val box = region.bounds
            val ring = listOf(
                Position(box.west, box.south),
                Position(box.east, box.south),
                Position(box.east, box.north),
                Position(box.west, box.north),
                Position(box.west, box.south)
            )
            Feature(
                geometry = Polygon(listOf(ring)),
                properties = buildJsonObject { put(PROP_NAME, region.name) }
            )
        }
    )

private const val PROP_NAME = "name"
private const val FILL_OPACITY = 0.08f
