package com.example.rygg.core.gpx

import android.util.Xml
import com.example.rygg.core.gpx.model.Extras
import com.example.rygg.core.gpx.model.GpxBounds
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxMetadata
import com.example.rygg.core.gpx.model.GpxParseResult
import com.example.rygg.core.gpx.model.GpxPoint
import com.example.rygg.core.gpx.model.GpxTags
import com.example.rygg.core.gpx.model.RawXml
import com.example.rygg.core.gpx.model.Route
import com.example.rygg.core.gpx.model.Track
import com.example.rygg.core.gpx.model.TrackSegment
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject

class GpxParser @Inject constructor() {
    private val warnings = mutableListOf<String>()

    fun parse(input: InputStream): GpxParseResult {
        warnings.clear()
        val parser: XmlPullParser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            setInput(input.buffered(), null)
        }
        parser.nextTag()
        require(parser.name == GpxTags.GPX.tag) { "Not a gpx file" }
        val document = readGpx(parser)
        return GpxParseResult(document, warnings.toList())
    }

    private inline fun XmlPullParser.children(onChild: (String) -> Unit) {
        val depth = this.depth
        while (true) {
            val event = next()
            if (event == XmlPullParser.END_DOCUMENT) break
            if (event == XmlPullParser.END_TAG && this.depth == depth) break
            if (event == XmlPullParser.START_TAG) onChild(name)
        }
    }

    private fun readGpx(parser: XmlPullParser): GpxDocument {
        val version = parser.getAttributeValue(null, "version")
        val creator = parser.getAttributeValue(null, "creator")
        val namespaces = readNamespaces(parser)
        var metadata: GpxMetadata? = null
        val waypoints = mutableListOf<GpxPoint>()
        val routes = mutableListOf<Route>()
        val tracks = mutableListOf<Track>()
        val unknown = mutableListOf<RawXml>()
        var extensions: RawXml? = null

        parser.children { name ->
            when (name) {
                GpxTags.METADATA.tag -> metadata = readMetadata(parser)
                GpxTags.WPT.tag -> waypoints += readPoint(parser)
                GpxTags.RTE.tag -> routes += readRoute(parser)
                GpxTags.TRK.tag -> tracks += readTrack(parser)
                GpxTags.EXTENSIONS.tag -> extensions = captureRaw(parser)
                else -> unknown += captureRaw(parser)
            }
        }

        return GpxDocument(
            version = version,
            creator = creator,
            namespaces = namespaces,
            metadata = metadata,
            waypoints = waypoints,
            routes = routes,
            tracks = tracks,
            extras = Extras(unknown, extensions)
        )
    }

    private fun readNamespaces(parser: XmlPullParser): Map<String, String> {
        val namespaces = LinkedHashMap<String, String>()
        for (i in 0 until parser.getNamespaceCount(parser.depth)) {
            val prefix = parser.getNamespacePrefix(i) ?: ""
            namespaces[prefix] = parser.getNamespaceUri(i)
        }
        return namespaces
    }

    private fun readMetadata(parser: XmlPullParser): GpxMetadata {
        var name: String? = null
        var desc: String? = null
        var time: Instant? = null
        var bounds: GpxBounds? = null
        val unknown = mutableListOf<RawXml>()
        var extensions: RawXml? = null

        parser.children { child ->
            when (child) {
                GpxTags.NAME.tag -> name = parser.readText()
                GpxTags.DESC.tag -> desc = parser.readText()
                GpxTags.TIME.tag -> time = parseInstant(parser.readText())
                GpxTags.BOUNDS.tag -> bounds = readBounds(parser)
                GpxTags.EXTENSIONS.tag -> extensions = captureRaw(parser)
                else -> unknown += captureRaw(parser)
            }
        }

        return GpxMetadata(name, desc, time, bounds, Extras(unknown, extensions))
    }

    private fun readBounds(parser: XmlPullParser): GpxBounds? {
        val minLat = parser.getAttributeValue(null, "minlat")?.toDoubleOrNull()
        val minLon = parser.getAttributeValue(null, "minlon")?.toDoubleOrNull()
        val maxLat = parser.getAttributeValue(null, "maxlat")?.toDoubleOrNull()
        val maxLon = parser.getAttributeValue(null, "maxlon")?.toDoubleOrNull()
        parser.children { captureRaw(parser) }
        if (minLat == null || minLon == null || maxLat == null || maxLon == null) {
            warnings += "Invalid bounds"
            return null
        }
        return GpxBounds(minLat, minLon, maxLat, maxLon)
    }

    private fun readPoint(parser: XmlPullParser): GpxPoint {
        val lat = parser.getAttributeValue(null, "lat").toDouble()
        val lon = parser.getAttributeValue(null, "lon").toDouble()
        var ele: Double? = null
        var time: Instant? = null
        var name: String? = null
        var desc: String? = null
        var sym: String? = null
        var type: String? = null
        var cmt: String? = null
        val unknown = mutableListOf<RawXml>()
        var extensions: RawXml? = null

        parser.children { child ->
            when (child) {
                GpxTags.ELE.tag -> ele = parser.readText().toDoubleOrNull()
                GpxTags.TIME.tag -> time = parseInstant(parser.readText())
                GpxTags.NAME.tag -> name = parser.readText()
                GpxTags.DESC.tag -> desc = parser.readText()
                GpxTags.SYM.tag -> sym = parser.readText()
                GpxTags.TYPE.tag -> type = parser.readText()
                GpxTags.CMT.tag -> cmt = parser.readText()
                GpxTags.EXTENSIONS.tag -> extensions = captureRaw(parser)
                else -> unknown += captureRaw(parser)
            }
        }

        return GpxPoint(lat, lon, ele, time, name, desc, sym, type, cmt, Extras(unknown, extensions))
    }

    private fun readRoute(parser: XmlPullParser): Route {
        var name: String? = null
        var desc: String? = null
        var number: Int? = null
        val points = mutableListOf<GpxPoint>()
        val unknown = mutableListOf<RawXml>()
        var extensions: RawXml? = null

        parser.children { child ->
            when (child) {
                GpxTags.NAME.tag -> name = parser.readText()
                GpxTags.DESC.tag -> desc = parser.readText()
                GpxTags.NUMBER.tag -> number = parser.readText().toIntOrNull()
                GpxTags.RTEPT.tag -> points += readPoint(parser)
                GpxTags.EXTENSIONS.tag -> extensions = captureRaw(parser)
                else -> unknown += captureRaw(parser)
            }
        }

        return Route(name, desc, number, points, Extras(unknown, extensions))
    }

    private fun readTrack(parser: XmlPullParser): Track {
        var name: String? = null
        var desc: String? = null
        var type: String? = null
        var number: Int? = null
        val segments = mutableListOf<TrackSegment>()
        val unknown = mutableListOf<RawXml>()
        var extensions: RawXml? = null

        parser.children { child ->
            when (child) {
                GpxTags.NAME.tag -> name = parser.readText()
                GpxTags.DESC.tag -> desc = parser.readText()
                GpxTags.TYPE.tag -> type = parser.readText()
                GpxTags.NUMBER.tag -> number = parser.readText().toIntOrNull()
                GpxTags.TRKSEG.tag -> segments += readSegment(parser)
                GpxTags.EXTENSIONS.tag -> extensions = captureRaw(parser)
                else -> unknown += captureRaw(parser)
            }
        }

        return Track(name, desc, type, number, segments, Extras(unknown, extensions))
    }

    private fun readSegment(parser: XmlPullParser): TrackSegment {
        val points = mutableListOf<GpxPoint>()
        val unknown = mutableListOf<RawXml>()
        var extensions: RawXml? = null

        parser.children { child ->
            when (child) {
                GpxTags.TRKPT.tag -> points += readPoint(parser)
                GpxTags.EXTENSIONS.tag -> extensions = captureRaw(parser)
                else -> unknown += captureRaw(parser)
            }
        }

        return TrackSegment(points, Extras(unknown, extensions))
    }

    private fun captureRaw(parser: XmlPullParser): RawXml {
        val builder = StringBuilder()
        val depth = parser.depth
        writeStartTag(parser, builder)
        while (true) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> writeStartTag(parser, builder)
                XmlPullParser.TEXT -> builder.append(escapeText(parser.text))
                XmlPullParser.END_TAG -> {
                    builder.append("</").append(qualifiedName(parser)).append('>')
                    if (parser.depth == depth) break
                }

                XmlPullParser.END_DOCUMENT -> break
            }
        }
        return RawXml(builder.toString())
    }

    private fun writeStartTag(parser: XmlPullParser, builder: StringBuilder) {
        builder.append('<').append(qualifiedName(parser))
        val begin = parser.getNamespaceCount(parser.depth - 1)
        val end = parser.getNamespaceCount(parser.depth)
        for (i in begin until end) {
            val prefix = parser.getNamespacePrefix(i)
            val declaration = if (prefix != null) "xmlns:$prefix" else "xmlns"
            builder.append(' ').append(declaration)
                .append("=\"").append(escapeAttribute(parser.getNamespaceUri(i))).append('"')
        }
        for (i in 0 until parser.attributeCount) {
            val prefix = parser.getAttributePrefix(i)
            val attribute = if (prefix != null) "$prefix:${parser.getAttributeName(i)}" else parser.getAttributeName(i)
            builder.append(' ').append(attribute)
                .append("=\"").append(escapeAttribute(parser.getAttributeValue(i))).append('"')
        }
        builder.append('>')
    }

    private fun qualifiedName(parser: XmlPullParser): String {
        val prefix = parser.prefix
        return if (prefix != null) "$prefix:${parser.name}" else parser.name
    }

    private fun parseInstant(value: String): Instant? =
        runCatching { OffsetDateTime.parse(value).toInstant() }.getOrElse {
            warnings += "Invalid time: $value"
            null
        }

    private fun XmlPullParser.readText(): String = nextText().trim()

    private fun escapeText(value: String): String =
        value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private fun escapeAttribute(value: String): String =
        escapeText(value).replace("\"", "&quot;")
}
