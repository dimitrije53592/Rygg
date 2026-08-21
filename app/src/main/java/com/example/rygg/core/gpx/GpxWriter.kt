package com.example.rygg.core.gpx

import android.util.Xml
import com.example.rygg.core.gpx.model.GpxDocument
import com.example.rygg.core.gpx.model.GpxMetadata
import com.example.rygg.core.gpx.model.GpxPoint
import com.example.rygg.core.gpx.model.GpxTags
import com.example.rygg.core.gpx.model.Route
import com.example.rygg.core.gpx.model.Track
import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Reverse of GpxParser: serializes our domain GpxDocument back into a GPX 1.1 file.
class GpxWriter @Inject constructor() {
    fun write(document: GpxDocument): String {
        val writer = StringWriter()
        val serializer = Xml.newSerializer().apply {
            setOutput(writer)
            setFeature(INDENT_FEATURE, true)
            startDocument("UTF-8", true)
        }

        serializer.startTag(null, GpxTags.GPX.tag)
        serializer.attribute(null, "version", "1.1")
        serializer.attribute(null, "creator", document.creator ?: DEFAULT_CREATOR)
        serializer.attribute(null, "xmlns", GPX_NAMESPACE)

        document.metadata?.let { writeMetadata(serializer, it) }
        document.waypoints.forEach { writePoint(serializer, GpxTags.WPT.tag, it) }
        document.tracks.forEach { writeTrack(serializer, it) }
        document.routes.forEach { writeRoute(serializer, it) }

        serializer.endTag(null, GpxTags.GPX.tag)
        serializer.endDocument()
        return writer.toString()
    }

    private fun writeMetadata(serializer: XmlSerializer, metadata: GpxMetadata) {
        serializer.startTag(null, GpxTags.METADATA.tag)
        metadata.name?.let { textTag(serializer, GpxTags.NAME.tag, it) }
        metadata.desc?.let { textTag(serializer, GpxTags.DESC.tag, it) }
        metadata.time?.let { textTag(serializer, GpxTags.TIME.tag, TIME_FORMAT.format(it)) }
        serializer.endTag(null, GpxTags.METADATA.tag)
    }

    private fun writeTrack(serializer: XmlSerializer, track: Track) {
        serializer.startTag(null, GpxTags.TRK.tag)
        track.name?.let { textTag(serializer, GpxTags.NAME.tag, it) }
        track.desc?.let { textTag(serializer, GpxTags.DESC.tag, it) }
        track.type?.let { textTag(serializer, GpxTags.TYPE.tag, it) }
        track.segments.forEach { segment ->
            serializer.startTag(null, GpxTags.TRKSEG.tag)
            segment.points.forEach { writePoint(serializer, GpxTags.TRKPT.tag, it) }
            serializer.endTag(null, GpxTags.TRKSEG.tag)
        }
        serializer.endTag(null, GpxTags.TRK.tag)
    }

    private fun writeRoute(serializer: XmlSerializer, route: Route) {
        serializer.startTag(null, GpxTags.RTE.tag)
        route.name?.let { textTag(serializer, GpxTags.NAME.tag, it) }
        route.desc?.let { textTag(serializer, GpxTags.DESC.tag, it) }
        route.points.forEach { writePoint(serializer, GpxTags.RTEPT.tag, it) }
        serializer.endTag(null, GpxTags.RTE.tag)
    }

    private fun writePoint(serializer: XmlSerializer, tag: String, point: GpxPoint) {
        serializer.startTag(null, tag)
        serializer.attribute(null, "lat", point.lat.toString())
        serializer.attribute(null, "lon", point.lon.toString())
        point.ele?.let { textTag(serializer, GpxTags.ELE.tag, it.toString()) }
        point.time?.let { textTag(serializer, GpxTags.TIME.tag, TIME_FORMAT.format(it)) }
        point.name?.let { textTag(serializer, GpxTags.NAME.tag, it) }
        point.desc?.let { textTag(serializer, GpxTags.DESC.tag, it) }
        point.sym?.let { textTag(serializer, GpxTags.SYM.tag, it) }
        point.type?.let { textTag(serializer, GpxTags.TYPE.tag, it) }
        point.cmt?.let { textTag(serializer, GpxTags.CMT.tag, it) }
        serializer.endTag(null, tag)
    }

    private fun textTag(serializer: XmlSerializer, tag: String, value: String) {
        serializer.startTag(null, tag)
        serializer.text(value)
        serializer.endTag(null, tag)
    }

    private companion object {
        const val INDENT_FEATURE = "http://xmlpull.org/v1/doc/features.html#indent-output"
        const val GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1"
        const val DEFAULT_CREATOR = "Rygg"
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT
    }
}
