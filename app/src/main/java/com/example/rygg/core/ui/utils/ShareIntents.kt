package com.example.rygg.core.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.rygg.R

// Builds the shareable web link for a route. The link opens the route in the Rygg app
// (see the Details deep link in AppNavigation).
object RouteShareLinks {
    const val BASE = "https://rygg.app"

    // TODO(server): replace the local entryId with a server-issued share token once routes
    //  are uploaded on share, so the link resolves on a recipient's device too.
    fun buildUrl(entryId: Long): String = "$BASE/r/$entryId"
}

// Share a route as a plain-text deep link through the system share sheet.
fun Context.shareRouteLink(url: String, routeName: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getString(R.string.details_share_link_message, routeName, url))
    }
    startActivity(Intent.createChooser(intent, getString(R.string.details_share_chooser_title)))
}

// Share the route's .gpx file as an attachment through the system share sheet. Uses a generic
// binary MIME so the OS "Save to Files"/"Save to device" target appears alongside messaging apps
// (the FileProvider keeps the file's .gpx name). The recipient still gets a valid .gpx file.
fun Context.shareGpxFile(uri: Uri, routeName: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, routeName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(intent, getString(R.string.details_share_chooser_title)))
}
