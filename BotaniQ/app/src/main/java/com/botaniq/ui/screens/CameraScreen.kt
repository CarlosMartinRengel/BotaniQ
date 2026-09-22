package com.botaniq.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.botaniq.R
import com.botaniq.ui.camera.CameraUIState
import com.botaniq.ui.camera.CameraViewModel
import com.botaniq.ui.camera.ScannerMode
import com.botaniq.ui.components.CameraPreview
import java.io.File

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(),
    isFromForm: Boolean = false,
    onPhotoConfirmedForForm: (Uri) -> Unit = { _ -> },
    onAnalyzeWithAI: (Uri, ScannerMode) -> Unit = { _, _ -> },
    onNavigateToRegistration: (String, Uri) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val state = viewModel.uiState

    val noPermissionMessage = stringResource(R.string.camera_toast_no_permission)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(isGranted)
            if (!isGranted) {
                Toast.makeText(
                    context,
                    noPermissionMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.onPermissionResult(hasPermission)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isPermissionGranted) {
            if (state.capturedImageUri != null) {
                ImageConfirmationContent(
                    imageUri = state.capturedImageUri,
                    mode = state.selectedMode,
                    onRetry = { viewModel.clearCapturedImage() },
                    isFromForm = isFromForm,
                    onConfirm = {
                        if (isFromForm) {
                            onPhotoConfirmedForForm(state.capturedImageUri)
                        } else {
                            onAnalyzeWithAI(state.capturedImageUri, state.selectedMode)
                        }
                    }
                )
            } else {
                ScannerContent(
                    state = state,
                    onModeChange = { viewModel.setScannerMode(it) },
                    onImageObtained = { uri -> viewModel.onImageCaptured(uri) },
                    isFromForm = isFromForm
                )
            }
        } else {
            PermissionRequestContent(onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            })
        }

        if (state.isProcessing || state.recognizedSpecies != null || state.errorMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .widthIn(max = 420.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        when {
                            state.isProcessing -> {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.camera_ia_analyzing),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }

                            state.errorMsg != null -> {
                                ResultIcon(
                                    icon = Icons.Default.FlashOff,
                                    iconColor = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.camera_ia_oops),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.errorMsg.asString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedButton(
                                    onClick = { viewModel.clearCapturedImage() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.camera_mode_retryPhoto))
                                }
                            }

                            state.recognizedSpecies != null -> {
                                val porcentaje = ((state.recognitionConfidence ?: 0f) * 100).toInt()
                                val modeIcon = if (state.selectedMode == ScannerMode.RECOGNITION)
                                    Icons.Default.LocalFlorist else Icons.Default.MedicalServices

                                ResultIcon(
                                    icon = modeIcon,
                                    iconColor = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = state.recognizedSpecies.asString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(
                                        R.string.camera_ia_confidence,
                                        porcentaje
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                if (state.selectedMode == ScannerMode.RECOGNITION) {
                                    val speciesName = state.recognizedSpecies.asString()

                                    Button(
                                        onClick = {
                                            val uri = state.capturedImageUri
                                            if (uri != null) {
                                                onNavigateToRegistration(speciesName, uri)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.camera_ia_register_plant))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                OutlinedButton(
                                    onClick = { viewModel.clearCapturedImage() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.camera_mode_retryPhoto))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultIcon(icon: ImageVector, iconColor: Color) {
    Surface(
        shape = CircleShape,
        color = iconColor.copy(alpha = 0.15f),
        modifier = Modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun PermissionRequestContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ResultIcon(icon = Icons.Default.CameraAlt, iconColor = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.camera_mode_access),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.camera_mode_permission),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.camera_permission_denied_hint),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.camera_mode_open))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerContent(
    state: CameraUIState,
    onImageObtained: (Uri) -> Unit,
    isFromForm: Boolean,
    onModeChange: (ScannerMode) -> Unit
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onImageObtained(uri)
            }
        }
    )

    var isFlashEnabled by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CameraPreview(modifier = Modifier.fillMaxSize(), imageCapture = imageCapture)

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFromForm) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    ScannerMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.selectedMode == mode,
                            onClick = { onModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ScannerMode.entries.size
                            ),
                            label = { Text(stringResource(id = mode.titleRes)) }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isFlashEnabled = !isFlashEnabled },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = stringResource(R.string.content_desc_toggle_flash),
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(4.dp, Color.White, CircleShape)
                        .clickable {
                            imageCapture.flashMode =
                                if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF

                            takePhoto(
                                context = context,
                                imageCapture = imageCapture,
                                onPhotoCaptured = { photoFile ->
                                    onImageObtained(photoFile.toUri())
                                })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = stringResource(R.string.content_desc_open_gallery),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoCaptured: (File) -> Unit,
) {
    val photoFile = File(context.cacheDir, "botaniq_draft_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.d("CameraScreen", "Foto guardada con éxito en: ${photoFile.absolutePath}")
                onPhotoCaptured(photoFile)
            }

            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraScreen", "Error al tomar la foto", exc)
            }
        }
    )
}

@Composable
fun ImageConfirmationContent(
    imageUri: Uri,
    mode: ScannerMode,
    isFromForm: Boolean,
    onRetry: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.camera_mode_confirmImage),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(MaterialTheme.shapes.large)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(R.string.content_desc_captured_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.camera_mode_retryPhoto))
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isFromForm) stringResource(R.string.camera_mode_plantFormOnResult)
                    else if (mode == ScannerMode.RECOGNITION) stringResource(R.string.camera_mode_recognitionOnResult)
                    else stringResource(R.string.camera_mode_diagnosisOnResult)
                )
            }
        }
    }
}
