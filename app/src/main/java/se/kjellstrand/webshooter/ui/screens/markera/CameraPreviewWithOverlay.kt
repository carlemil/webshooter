package se.kjellstrand.webshooter.ui.screens.markera

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import se.kjellstrand.webshooter.data.vision.HoleDetector
import se.kjellstrand.webshooter.data.vision.filterByConfidence
import se.kjellstrand.webshooter.data.vision.mapToImageSpace
import se.kjellstrand.webshooter.data.vision.nonMaxSuppression
import java.util.concurrent.Executors

private const val TAG = "Markera"
private const val CONFIDENCE_THRESHOLD = 0.35f
private const val IOU_THRESHOLD = 0.45f

@Composable
fun CameraPreviewWithOverlay(
    onDetections: (detections: List<se.kjellstrand.webshooter.data.vision.Detection>, imageWidth: Int, imageHeight: Int) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detector: HoleDetector = koinInject()

    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val inferenceScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
                    )
                    .build()
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .apply {
                        setAnalyzer(analyzerExecutor) { proxy ->
                            try {
                                val bitmap = proxy.toUprightBitmap()
                                val srcW = bitmap.width
                                val srcH = bitmap.height
                                val input = bitmap.toModelInput(detector.inputSize)
                                bitmap.recycle()
                                inferenceScope.launch {
                                    try {
                                        val raws = detector.detect(input)
                                        val kept = nonMaxSuppression(
                                            filterByConfidence(raws, CONFIDENCE_THRESHOLD),
                                            IOU_THRESHOLD,
                                        )
                                        val detections = mapToImageSpace(kept, detector.inputSize, srcW, srcH)
                                        onDetections(detections, srcW, srcH)
                                    } catch (t: Throwable) {
                                        Napier.w("frame inference failed", t, tag = TAG)
                                    }
                                }
                            } catch (t: Throwable) {
                                Napier.w("frame preprocessing failed", t, tag = TAG)
                            } finally {
                                proxy.close()
                            }
                        }
                    }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            } catch (t: Throwable) {
                Napier.e("CameraX bind failed", t, tag = TAG)
                onError(t)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            inferenceScope.cancel()
            analyzerExecutor.shutdown()
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (_: Throwable) {
                // best-effort cleanup
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
