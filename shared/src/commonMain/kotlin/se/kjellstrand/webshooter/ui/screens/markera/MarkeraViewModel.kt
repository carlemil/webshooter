package se.kjellstrand.webshooter.ui.screens.markera

import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.vision.Detection

interface MarkeraViewModel {
    val uiState: StateFlow<MarkeraUiState>

    /** Push the result of one CameraX frame's analysis. */
    fun onFrameAnalysed(detections: List<Detection>, imageWidth: Int, imageHeight: Int)

    /** Push the result of a single gallery-pick inference. */
    fun onGalleryImageAnalysed(detections: List<Detection>, imageWidth: Int, imageHeight: Int)

    fun switchMode(mode: MarkeraMode)

    fun setProcessing(isProcessing: Boolean)

    fun setError(message: String?)
}
