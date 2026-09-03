package com.example.rygg.core.navigation

// Internal-only deep links fired by explicit intents (e.g. notification actions -> MainActivity).
// Custom scheme so nothing outside the app can match them; no manifest <intent-filter> needed
// because the intents name MainActivity directly. Contrast RouteShareLinks.BASE, whose https App
// Link arrives from the browser and therefore does need a filter.
object InternalDeepLinks {
    const val BASE = "rygg://rygg.app"

    // Opens the live recording screen (tapping the ongoing-recording notification body).
    const val RECORD = "$BASE/record"

    // Opens the post-recording save/preview screen (RecordingPreview).
    const val RECORDING_PREVIEW = "$BASE/recording-preview"
}
