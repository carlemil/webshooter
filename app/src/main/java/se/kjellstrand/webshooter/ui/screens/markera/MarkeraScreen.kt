package se.kjellstrand.webshooter.ui.screens.markera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
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
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import se.kjellstrand.webshooter.data.vision.HoleDetector
import se.kjellstrand.webshooter.data.vision.filterByConfidence
import se.kjellstrand.webshooter.data.vision.mapToImageSpace
import se.kjellstrand.webshooter.data.vision.nonMaxSuppression
import se.kjellstrand.webshooter.resources.Res
import se.kjellstrand.webshooter.resources.markera_camera_permission_required
import se.kjellstrand.webshooter.resources.markera_detect
import se.kjellstrand.webshooter.resources.markera_error_image_decode
import se.kjellstrand.webshooter.resources.markera_error_inference
import se.kjellstrand.webshooter.resources.markera_pick_image
import se.kjellstrand.webshooter.resources.markera_switch_to_camera

private const val TAG = "Markera"
private const val CONFIDENCE_THRESHOLD = 0.35f
private const val IOU_THRESHOLD = 0.45f

@Composable
fun MarkeraScreen() {
    val context = LocalContext.current
    val viewModel: MarkeraViewModelImpl = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val detector: HoleDetector = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val pickedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
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

    val errorImageDecode = stringResource(Res.string.markera_error_image_decode)
    val errorInference = stringResource(Res.string.markera_error_inference)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.switchMode(MarkeraMode.Gallery)
        viewModel.setProcessing(true)
        coroutineScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                if (bitmap == null) {
                    viewModel.setError(errorImageDecode)
                    return@launch
                }
                val input = withContext(Dispatchers.Default) {
                    bitmap.toModelInput(detector.inputSize)
                }
                val raws = detector.detect(input)
                val kept = nonMaxSuppression(
                    filterByConfidence(raws, CONFIDENCE_THRESHOLD),
                    IOU_THRESHOLD,
                )
                val detections = mapToImageSpace(kept, detector.inputSize, bitmap.width, bitmap.height)
                pickedBitmap.value = bitmap
                viewModel.onGalleryImageAnalysed(detections, bitmap.width, bitmap.height)
            } catch (t: Throwable) {
                Napier.w("gallery inference failed", t, tag = TAG)
                viewModel.setError(errorInference)
            }
        }
    }

    val onDetectClick: () -> Unit = onDetect@{
        if (uiState.isProcessing) return@onDetect
        val snapshot = previewView.bitmap ?: return@onDetect
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
                viewModel.onFrameAnalysed(detections, snapshot.width, snapshot.height)
            } catch (t: Throwable) {
                Napier.w("snapshot inference failed", t, tag = TAG)
                viewModel.setError(errorInference)
            } finally {
                viewModel.setProcessing(false)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.mode) {
            MarkeraMode.Camera -> {
                if (cameraGranted) {
                    CameraPreview(
                        previewView = previewView,
                        onError = { viewModel.setError(it.message) },
                        modifier = Modifier.fillMaxSize(),
                    )
                    DetectionOverlay(
                        detections = uiState.detections,
                        imageWidth = uiState.imageWidth,
                        imageHeight = uiState.imageHeight,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    PermissionPrompt(onGrantClick = { permissionLauncher.launch(Manifest.permission.CAMERA) })
                }
            }
            MarkeraMode.Gallery -> {
                val bmp = pickedBitmap.value
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                DetectionOverlay(
                    detections = uiState.detections,
                    imageWidth = uiState.imageWidth,
                    imageHeight = uiState.imageHeight,
                    modifier = Modifier.fillMaxSize(),
                )
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            if (uiState.mode == MarkeraMode.Camera && cameraGranted) {
                FloatingActionButton(onClick = onDetectClick) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.markera_detect))
                }
            }
            if (uiState.mode == MarkeraMode.Gallery) {
                FloatingActionButton(onClick = {
                    pickedBitmap.value = null
                    viewModel.switchMode(MarkeraMode.Camera)
                }) {
                    Icon(Icons.Default.Videocam, contentDescription = stringResource(Res.string.markera_switch_to_camera))
                }
            }
            FloatingActionButton(onClick = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = stringResource(Res.string.markera_pick_image))
            }
        }
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
                Text(stringResource(Res.string.markera_pick_image))
            }
        }
    }
}
