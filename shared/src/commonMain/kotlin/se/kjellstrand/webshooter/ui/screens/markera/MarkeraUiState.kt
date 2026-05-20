package se.kjellstrand.webshooter.ui.screens.markera

import se.kjellstrand.webshooter.data.vision.Detection

enum class MarkeraMode { Camera, Gallery }

data class MarkeraUiState(
    val mode: MarkeraMode = MarkeraMode.Camera,
    val detections: List<Detection> = emptyList(),
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val isProcessing: Boolean = false,
    val error: String? = null,
)
