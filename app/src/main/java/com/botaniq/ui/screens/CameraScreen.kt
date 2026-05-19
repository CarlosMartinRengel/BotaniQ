package com.botaniq.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    onPhotoConfirmedForForm: (Uri) -> Unit = {},
    onAnalyzeWithAI: (Uri, ScannerMode) -> Unit = { _, _ -> },
    onNavigateToRegistration: (String, Uri) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val state = viewModel.uiState

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(isGranted)
            if (!isGranted) {
                makeText(
                    context,
                    "Sin permiso de cámara",
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
    //1. Capa normal
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

        // 2. Pantalla de carga y Resultados de la IA
        if (state.isProcessing || state.recognizedSpecies != null || state.errorMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)), // Fondo oscurecido elegante
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // Ocupa el 80% del ancho
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(24.dp)
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "La IA está analizando la planta...",
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    } else if (state.errorMsg != null) {
                        Icon(
                            imageVector = Icons.Default.FlashOff,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡Ups!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(state.errorMsg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.clearCapturedImage() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.camera_mode_retryPhoto))
                        }
                    } else if (state.recognizedSpecies != null) {
                        val porcentaje = ((state.recognitionConfidence ?: 0f) * 100).toInt()

                        Icon(
                            imageVector = Icons.Default.PhotoLibrary, // O el icono de planta que prefieras
                            contentDescription = "Éxito",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.recognizedSpecies,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Confianza: $porcentaje%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val species = state.recognizedSpecies ?: ""
                                val uri = state.capturedImageUri
                                if (uri != null) {
                                    onNavigateToRegistration(species, uri)
                                }

                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Registrar planta")
                        }
                        Button(
                            onClick = {
                                viewModel.clearCapturedImage()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver a intentar")
                        }
                    }
                }
            }
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
        Text(
            stringResource(R.string.camera_mode_access),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.camera_mode_permission),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Si denegaste el permiso previamente, debes activarlo desde los Ajustes de tu teléfono.",
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
                // Selector de modo (Reconocimiento / Diagnóstico)
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ScannerMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.selectedMode == mode,
                            onClick = { onModeChange(mode) },
                            label = {
                                Text(
                                    text = stringResource(id = mode.titleRes),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
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
                    // Le damos un fondo semi-transparente para que se vea sobre cualquier planta
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Alternar Flash",
                        tint = Color.White
                    )
                }
                Button(
                    onClick = {
                        imageCapture.flashMode =
                            if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF

                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onPhotoCaptured = { photoFile ->
                                onImageObtained(photoFile.toUri())
                            })
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bottom_camera_focused),
                        contentDescription = "icon",
                        tint = contentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            // Solo imagenes
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Abrir Galería",
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
                onPhotoCaptured(photoFile) // El archivo se devuelve a la UI
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Cabecera
        Text(
            text = stringResource(R.string.camera_mode_confirmImage),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 32.dp)
        )

        // 2. Imagen capturada
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Imagen capturada",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Botonera (Reintentar / Continuar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text(stringResource(R.string.camera_mode_retryPhoto))
            }

            Button(onClick = onConfirm) {
                Text(
                    text = if (isFromForm) stringResource(R.string.camera_mode_plantFormOnResult)
                    else if (mode == ScannerMode.RECOGNITION) stringResource(R.string.camera_mode_recognitionOnResult)
                    else stringResource(R.string.camera_mode_diagnosisOnResult)
                )
            }
            //TODO internacionalizacion
        }
    }
}



