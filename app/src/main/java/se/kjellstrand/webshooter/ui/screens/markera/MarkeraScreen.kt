package se.kjellstrand.webshooter.ui.screens.markera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import se.kjellstrand.webshooter.data.vision.Detection
import se.kjellstrand.webshooter.data.vision.HitScore
import se.kjellstrand.webshooter.data.vision.HoleDetector
import se.kjellstrand.webshooter.data.vision.TARGET_CARD_WIDTH_MM
import se.kjellstrand.webshooter.data.vision.TargetCalibration
import se.kjellstrand.webshooter.data.vision.computeHitScores
import se.kjellstrand.webshooter.data.vision.filterByConfidence
import se.kjellstrand.webshooter.data.vision.mapToImageSpace
import se.kjellstrand.webshooter.data.vision.nonMaxSuppression
import se.kjellstrand.webshooter.resources.Res
import se.kjellstrand.webshooter.resources.markera_camera_permission_required
import se.kjellstrand.webshooter.resources.markera_detect
import se.kjellstrand.webshooter.resources.markera_error_inference
import se.kjellstrand.webshooter.resources.markera_grant_permission
import se.kjellstrand.webshooter.resources.markera_resume_live

private const val TAG = "Markera"
private const val SCORE_TAG = "MarkeraScore"
private const val CONFIDENCE_THRESHOLD = 0.35f
private const val IOU_THRESHOLD = 0.45f

private fun scoreDetections(
    detections: List<Detection>,
    width: Int,
    height: Int,
    calibration: TargetCalibration?,
): List<HitScore> =
    if (calibration != null) {
        computeHitScores(detections, calibration)
    } else {
        computeHitScores(
            detections,
            centerX = width / 2f,
            centerY = height / 2f,
            mmPerPx = TARGET_CARD_WIDTH_MM / width.toDouble(),
        )
    }

private fun logHitScores(scores: List<HitScore>, calibration: TargetCalibration?) {
    if (calibration != null) {
        Napier.d(
            "calibrated: centre=(${calibration.centerX.toInt()},${calibration.centerY.toInt()}) " +
                "semiMajor=${calibration.semiMajorPx.toInt()}px " +
                "semiMinor=${calibration.semiMinorPx.toInt()}px " +
                "θ=${"%.1f".format(calibration.rotationRad * 180.0 / kotlin.math.PI)}° " +
                "mmPerPx=${"%.3f".format(calibration.mmPerPx)} " +
                "conf=${"%.2f".format(calibration.confidence)}",
            tag = SCORE_TAG,
        )
    } else {
        Napier.d("fallback calibration (no 7-ring blob found)", tag = SCORE_TAG)
    }
    val total = scores.sumOf { if (it.isInnerTen) 10 else it.ring }
    scores.forEachIndexed { i, s ->
        val ringStr = if (s.isInnerTen) "X" else s.ring.toString()
        Napier.d(
            "hit #${i + 1}: ring=$ringStr distance=${"%.1f".format(s.distanceMm)}mm " +
                "px=(${s.centerXpx.toInt()},${s.centerYpx.toInt()})",
            tag = SCORE_TAG,
        )
    }
    Napier.d("total: ${scores.size} hits, score=$total", tag = SCORE_TAG)
}

/** Top-5 hits (already sorted desc by [computeHitScores]) → picker indices, padded to 5 with 0. */
private fun topPickerValues(scores: List<HitScore>): List<Int> {
    val taken = scores.take(SCORE_PICKER_COUNT).map {
        if (it.isInnerTen) SCORE_PICKER_INNER_TEN else it.ring
    }
    return taken + List(SCORE_PICKER_COUNT - taken.size) { 0 }
}

@Composable
fun MarkeraScreen() {
    val context = LocalContext.current
    val viewModel: MarkeraViewModelImpl = koinViewModel()
    val snapshotVm: MarkeraSnapshotViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val detector: HoleDetector = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> cameraGranted = granted }

    LaunchedEffect(Unit) {
        if (!cameraGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val errorInference = stringResource(Res.string.markera_error_inference)

    val onDetectClick: () -> Unit = onDetect@{
        if (uiState.isProcessing) return@onDetect
        val snapshot = previewView.bitmap ?: return@onDetect
        // Freeze the frame immediately so the user sees the static image
        // the model will analyse instead of the live preview.
        snapshotVm.set(snapshot)
        viewModel.setProcessing(true)
        coroutineScope.launch {
            try {
                val input = withContext(Dispatchers.Default) {
                    snapshot.toModelInput(detector.inputSize)
                }
                val raws = detector.detect(input)
                val kept = nonMaxSuppression(
                    filterByConfidence(raws, CONFIDENCE_THRESHOLD),
                    IOU_THRESHOLD,
                )
                val detections = mapToImageSpace(kept, detector.inputSize, snapshot.width, snapshot.height)
                Napier.d("snapshot ${snapshot.width}x${snapshot.height}: raw=${raws.size} kept=${kept.size}", tag = TAG)
                val freshCal = withContext(Dispatchers.Default) { snapshot.calibrate() }
                Napier.d(
                    "snapshot dims ${snapshot.width}x${snapshot.height}, calibration=" +
                        (freshCal?.let {
                            "(${it.centerX.toInt()},${it.centerY.toInt()}) " +
                                "a=${it.semiMajorPx.toInt()}px b=${it.semiMinorPx.toInt()}px"
                        } ?: "null"),
                    tag = TAG,
                )
                viewModel.setCalibration(freshCal)
                val effectiveCal = freshCal ?: uiState.calibration
                val scores = scoreDetections(detections, snapshot.width, snapshot.height, effectiveCal)
                logHitScores(scores, effectiveCal)
                viewModel.onFrameAnalysed(detections, snapshot.width, snapshot.height)
                viewModel.setTopScores(topPickerValues(scores))
            } catch (t: Throwable) {
                Napier.w("snapshot inference failed", t, tag = TAG)
                viewModel.setError(errorInference)
            }
        }
    }

    val onResumeLive: () -> Unit = {
        snapshotVm.clear()
        viewModel.clearResults()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isPortrait = maxHeight >= maxWidth

        if (!cameraGranted) {
            PermissionPrompt(
                onGrantClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
        } else {
            // Pickers and viewport live in disjoint slots so they never
            // overlap: portrait stacks them vertically, landscape places
            // pickers to the left of the viewport.
            if (isPortrait) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ScorePickerHorizontalRow(
                        values = uiState.topScores,
                        onValueChange = viewModel::setTopScoreAt,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                    )
                    Viewport(
                        snapshotVm = snapshotVm,
                        uiState = uiState,
                        previewView = previewView,
                        onError = { viewModel.setError(it.message) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(Alignment.CenterHorizontally),
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    ScorePickerVerticalColumn(
                        values = uiState.topScores,
                        onValueChange = viewModel::setTopScoreAt,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(16.dp),
                    )
                    Viewport(
                        snapshotVm = snapshotVm,
                        uiState = uiState,
                        previewView = previewView,
                        onError = { viewModel.setError(it.message) },
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .align(Alignment.CenterVertically),
                    )
                }
            }
        }

        uiState.error?.let { msg ->
            Text(
                text = msg,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(8.dp),
            )
        }

        if (cameraGranted) {
            val isFrozen = snapshotVm.snapshot != null
            FloatingActionButton(
                onClick = if (isFrozen) onResumeLive else onDetectClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                if (isFrozen) {
                    Icon(
                        Icons.Default.Videocam,
                        contentDescription = stringResource(Res.string.markera_resume_live),
                    )
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(Res.string.markera_detect),
                    )
                }
            }
        }
    }
}

@Composable
private fun Viewport(
    snapshotVm: MarkeraSnapshotViewModel,
    uiState: MarkeraUiState,
    previewView: PreviewView,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        val frozen = snapshotVm.snapshot
        if (frozen != null) {
            Image(
                bitmap = frozen.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CameraPreview(
                previewView = previewView,
                onError = onError,
                modifier = Modifier.fillMaxSize(),
            )
        }
        CalibrationOverlay(
            calibration = uiState.calibration,
            imageWidth = uiState.imageWidth,
            imageHeight = uiState.imageHeight,
            modifier = Modifier.fillMaxSize(),
        )
        DetectionOverlay(
            detections = uiState.detections,
            imageWidth = uiState.imageWidth,
            imageHeight = uiState.imageHeight,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PermissionPrompt(onGrantClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = stringResource(Res.string.markera_camera_permission_required),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onGrantClick) {
                Text(stringResource(Res.string.markera_grant_permission))
            }
        }
    }
}
