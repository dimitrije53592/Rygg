package com.example.rygg.core.gpx

import android.util.Xml
import com.example.rygg.core.gpx.model.Extras
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxMetadata
import com.example.rygg.core.gpx.model.GpxParseResult
import com.example.rygg.core.gpx.model.GpxPoint
import com.example.rygg.core.gpx.model.GpxTags
import com.example.rygg.core.gpx.model.RawXml
import com.example.rygg.core.gpx.model.Route
import com.example.rygg.core.gpx.model.Track
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import javax.inject.Inject

class GpxParser @Inject constructor() {
    fun parse(input: InputStream): GpxParseResult {

        val parser: XmlPullParser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            setInput(input.buffered(), null)
        }
        parser.next()
        require(parser.name == GpxTags.GPX.name) { "Not a gpx file" }
        return GpxParseResult(readGpx(parser), warnings)
    }

    private inline fun XmlPullParser.children(onChild: (String) -> Unit) {
        val depth = this.depth
        if (next() != XmlPullParser.END_TAG || this.depth > depth) {
            if (eventType == XmlPullParser.START_TAG) onChild(name)
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
        var extensions: RawXml? = null

        parser.children { name ->
            when (name) {
                GpxTags.METADATA.name -> metadata = readMetadata(parser)
                GpxTags.WPT.name -> waypoints += readWaypoints(parser)
                GpxTags.RTE.name -> routes += readRoute(parser)
                GpxTags.TRK.name -> tracks += readTrack(parser)
                GpxTags.EXTENSIONS.name -> captureRaw(parser)
                else -> captureRaw(parser)
            }
        }

        return GpxDocument(
            version = version,
            creator = creator
        )
    }

    private fun readNamespaces(parser: XmlPullParser): Map<String, String> {

    }

    private fun readMetadata(parser: XmlPullParser): GpxMetadata {

    }

    private fun readWaypoints(parser: XmlPullParser): List<GpxPoint> {

    }

    private fun readRoute(parser: XmlPullParser): Route {

    }

    private fun readTrack(parser: XmlPullParser): Track {

    }

    private fun captureRaw(parser: XmlPullParser): Extras {

    }
}
