package com.dimitrijeilic.rygg.core.gpx

import android.util.Xml
import com.dimitrijeilic.rygg.core.gpx.model.Extras
import com.dimitrijeilic.rygg.core.gpx.model.GpxBounds
import com.dimitrijeilic.rygg.core.gpx.model.GpxDocument
import com.dimitrijeilic.rygg.core.gpx.model.GpxMetadata
import com.dimitrijeilic.rygg.core.gpx.model.GpxPoint
import com.dimitrijeilic.rygg.core.gpx.model.GpxTags
import com.dimitrijeilic.rygg.core.gpx.model.RawXml
import com.dimitrijeilic.rygg.core.gpx.model.Route
import com.dimitrijeilic.rygg.core.gpx.model.Track
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
        val output = Output(serializer, writer)

        serializer.startTag(null, GpxTags.GPX.tag)
        serializer.attribute(null, "version", "1.1")
        serializer.attribute(null, "creator", document.creator ?: DEFAULT_CREATOR)
        serializer.attribute(null, "xmlns", GPX_NAMESPACE)
        document.namespaces
            .filterKeys { it.isNotEmpty() }
            .forEach { (prefix, uri) -> serializer.attribute(null, "xmlns:$prefix", uri) }

        writeMetadata(output, document.metadata ?: GpxMetadata(), document.computeBounds())
        document.waypoints.forEach { writePoint(output, GpxTags.WPT.tag, it) }
        document.routes.forEach { writeRoute(output, it) }
        document.tracks.forEach { writeTrack(output, it) }
        writeExtras(output, document.extras)

        serializer.endTag(null, GpxTags.GPX.tag)
        serializer.endDocument()
        return writer.toString()
    }

    private fun writeMetadata(output: Output, metadata: GpxMetadata, bounds: GpxBounds?) {
        val serializer = output.serializer
        serializer.startTag(null, GpxTags.METADATA.tag)
        metadata.name?.let { textTag(serializer, GpxTags.NAME.tag, it) }
        metadata.desc?.let { textTag(serializer, GpxTags.DESC.tag, it) }
        metadata.extras.unknownChildren.forEach { output.raw(it) }
        metadata.time?.let { textTag(serializer, GpxTags.TIME.tag, TIME_FORMAT.format(it)) }
        (bounds ?: metadata.bounds)?.let { writeBounds(serializer, it) }
        metadata.extras.extensions?.let { output.raw(it) }
        serializer.endTag(null, GpxTags.METADATA.tag)
    }

    private fun writeBounds(serializer: XmlSerializer, bounds: GpxBounds) {
        serializer.startTag(null, GpxTags.BOUNDS.tag)
        serializer.attribute(null, "minlat", bounds.minLat.toString())
        serializer.attribute(null, "minlon", bounds.minLon.toString())
        serializer.attribute(null, "maxlat", bounds.maxLat.toString())
        serializer.attribute(null, "maxlon", bounds.maxLon.toString())
        serializer.endTag(null, GpxTags.BOUNDS.tag)
    }

    private fun writeTrack(output: Output, track: Track) {
        val serializer = output.serializer
        serializer.startTag(null, GpxTags.TRK.tag)
        track.name?.let { textTag(serializer, GpxTags.NAME.tag, it) }
        track.desc?.let { textTag(serializer, GpxTags.DESC.tag, it) }
        track.extras.unknownChildren.forEach { output.raw(it) }
        track.number?.let { textTag(serializer, GpxTags.NUMBER.tag, it.toString()) }
        track.type?.let { textTag(serializer, GpxTags.TYPE.tag, it) }
        track.extras.extensions?.let { output.raw(it) }
        track.segments.forEach { segment ->
            serializer.startTag(null, GpxTags.TRKSEG.tag)
            segment.points.forEach { writePoint(output, GpxTags.TRKPT.tag, it) }
            writeExtras(output, segment.extras)
            serializer.endTag(null, GpxTags.TRKSEG.tag)
        }
        serializer.endTag(null, GpxTags.TRK.tag)
    }

    private fun writeRoute(output: Output, route: Route) {
        val serializer = output.serializer
        serializer.startTag(null, GpxTags.RTE.tag)
        route.name?.let { textTag(serializer, GpxTags.NAME.tag, it) }
        route.desc?.let { textTag(serializer, GpxTags.DESC.tag, it) }
        route.extras.unknownChildren.forEach { output.raw(it) }
        route.number?.let { textTag(serializer, GpxTags.NUMBER.tag, it.toString()) }
        route.extras.extensions?.let { output.raw(it) }
        route.points.forEach { writePoint(output, GpxTags.RTEPT.tag, it) }
        serializer.endTag(null, GpxTags.RTE.tag)
    }

    private fun writePoint(output: Output, tag: String, point: GpxPoint) {
        val serializer = output.serializer
        serializer.startTag(null, tag)
        serializer.attribute(null, "lat", point.lat.toString())
        serializer.attribute(null, "lon", point.lon.toString())
        point.ele?.let { textTag(serializer, GpxTags.ELE.tag, it.toString()) }
        point.time?.let { textTag(serializer, GpxTags.TIME.tag, TIME_FORMAT.format(it)) }
        point.name?.let { textTag(serializer, GpxTags.NAME.tag, it) }
        point.cmt?.let { textTag(serializer, GpxTags.CMT.tag, it) }
        point.desc?.let { textTag(serializer, GpxTags.DESC.tag, it) }
        point.sym?.let { textTag(serializer, GpxTags.SYM.tag, it) }
        point.type?.let { textTag(serializer, GpxTags.TYPE.tag, it) }
        writeExtras(output, point.extras)
        serializer.endTag(null, tag)
    }

    private fun writeExtras(output: Output, extras: Extras) {
        extras.unknownChildren.forEach { output.raw(it) }
        extras.extensions?.let { output.raw(it) }
    }

    private fun textTag(serializer: XmlSerializer, tag: String, value: String) {
        serializer.startTag(null, tag)
        serializer.text(value)
        serializer.endTag(null, tag)
    }

    private fun GpxDocument.computeBounds(): GpxBounds? {
        val points = waypoints + routes.flatMap { it.points } +
            tracks.flatMap { track -> track.segments.flatMap { it.points } }
        if (points.isEmpty()) return null
        return GpxBounds(
            minLat = points.minOf { it.lat },
            minLon = points.minOf { it.lon },
            maxLat = points.maxOf { it.lat },
            maxLon = points.maxOf { it.lon }
        )
    }

    private class Output(val serializer: XmlSerializer, private val writer: StringWriter) {
        fun raw(xml: RawXml) {
            serializer.flush()
            writer.write(xml.value)
        }
    }

    private companion object {
        const val INDENT_FEATURE = "http://xmlpull.org/v1/doc/features.html#indent-output"
        const val GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1"
        const val DEFAULT_CREATOR = "Rygg"
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT
    }
}
