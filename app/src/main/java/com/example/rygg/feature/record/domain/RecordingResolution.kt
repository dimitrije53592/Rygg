package com.example.rygg.feature.record.domain

import androidx.annotation.StringRes
import com.example.rygg.R

// How densely a recording stores track points. GPS is always polled at 1 Hz (auto-pause needs
// that rate to detect a stop); this only decides which of those fixes are kept in the track, so
// it trades file size and detail, not battery. Every maxIntervalSeconds stays well under the
// analyzer's 60 s pause gap, so a thinned stretch is never misread as a pause.
enum class RecordingResolution(
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val minDistanceMeters: Double,
    val maxIntervalSeconds: Long
) {
    DETAILED(R.string.settings_recording_detailed, R.string.settings_recording_detailed_desc, 5.0, 10),
    BALANCED(R.string.settings_recording_balanced, R.string.settings_recording_balanced_desc, 10.0, 20),
    COMPACT(R.string.settings_recording_compact, R.string.settings_recording_compact_desc, 25.0, 30)
}
