package se.kjellstrand.webshooter.ui.screens.markera

import se.kjellstrand.webshooter.data.vision.Detection
import se.kjellstrand.webshooter.data.vision.TargetCalibration

data class MarkeraUiState(
    val detections: List<Detection> = emptyList(),
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val calibration: TargetCalibration? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
)
