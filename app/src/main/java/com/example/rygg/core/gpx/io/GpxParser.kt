package com.example.rygg.core.gpx.io

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class GpxParser {
    companion object {
        fun parse(inputStream: InputStream) {
            inputStream.use { inputStream ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setInput(inputStream, null)

                val list = readFile(parser)
                Log.d("Sofija", "KRAJ -> $list")
            }
        }

        private fun readFile(parser: XmlPullParser): List<String> {
            val waypoints = mutableListOf<String>()

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType != XmlPullParser.START_DOCUMENT) continue

                when (parser.name) {
                    "trk" -> readTrack(parser)
                    "wpt" -> waypoints += readPoint(parser)
                    "rte" -> readRoute(parser)
                    else -> capture(parser)
                }
            }

            return waypoints
        }

        private fun readTrack(parser: XmlPullParser) {
            Log.d("Sofija", "TRK ${parser.attributeCount}")
        }

        private fun readPoint(parser: XmlPullParser): String {
            Log.d("Sofija", "WPT ${parser.attributeCount}")
            return parser.text
        }

        private fun readRoute(parser: XmlPullParser) {
            Log.d("Sofija", "RTE ${parser.attributeCount}")
        }

        private fun capture(parser: XmlPullParser) {

        }
    }
}