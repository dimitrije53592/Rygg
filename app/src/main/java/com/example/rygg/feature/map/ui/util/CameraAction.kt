package com.example.rygg.feature.map.ui.util

sealed interface CameraAction {
    data class AnimateTo(
        val longitude: Double,
        val latitude: Double,
        val bearing: Double,
        val zoom: Double = PREVIEW_ZOOM,
        val tilt: Double = PREVIEW_TILT,
        val duration: Long = PREVIEW_INTRO_MS
    ) : CameraAction

    data class PositionTo(
        val longitude: Double,
        val latitude: Double,
        val bearing: Double,
        val zoom: Double = PREVIEW_ZOOM,
        val tilt: Double = PREVIEW_TILT
    ) : CameraAction
}

private const val PREVIEW_ZOOM = 15.0
private const val PREVIEW_TILT = 45.0
private const val PREVIEW_INTRO_MS = 1200L
