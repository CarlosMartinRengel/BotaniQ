package com.botaniq.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.botaniq.R
import com.botaniq.ui.components.DeleteDialog
import com.botaniq.ui.plantdetail.PlantFormViewModel
import com.botaniq.ui.plantdetail.WeatherAnalysisStatus
import com.botaniq.utils.UiText
import com.botaniq.utils.formatDate

@Composable
fun PlantDetailEditScreen(
    plantId: Int,
    onBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    navController: NavHostController,
    viewModel: PlantFormViewModel
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val currentEntry by navController.currentBackStackEntryAsState()

    val scrollState = rememberScrollState()

    val returnedUriState = currentEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("returnedPhotoUri", null)
        ?.collectAsState()

    val returnedUri = returnedUriState?.value

    LaunchedEffect(returnedUri) {
        returnedUri?.let { uri ->
            if (uri.isNotEmpty()) {
                viewModel.onPhotoUriChange(uri, context)
                currentEntry?.savedStateHandle?.set("returnedPhotoUri", null)
            }
        }
    }

    LaunchedEffect(plantId) {
        if (plantId != -1) {
            viewModel.loadExistingPlant(plantId)
        }
    }

    if (showDeleteConfirmDialog) {
        DeleteDialog(
            title = stringResource(R.string.dialog_delete_title_single),
            message = stringResource(R.string.dialog_delete_message_single, state.nickname),
            onConfirm = { viewModel.deletePlant(onSuccess = onBack) },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back)
                    )
                }
                Text(
                    text = if (state.isEditMode) stringResource(R.string.plant_details_topBar_editPlant)
                    else stringResource(R.string.plant_details_topBar_details),
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            if (state.isEditMode) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = stringResource(R.string.content_desc_edit)
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.content_desc_delete_plant),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .graphicsLayer {
                        translationY = scrollState.value * 0.5f
                    }
            ) {
                AsyncImage(
                    model = state.photoUri ?: R.drawable.botaniq_launcher_foreground,
                    contentDescription = stringResource(R.string.content_desc_plant_photo),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                if (state.isEditMode) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .clickable { onNavigateToCamera() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.content_desc_edit_photo),
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = state.nickname,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = "${state.commonName} (${state.speciesName})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                if (state.isEditMode) {
                    OutlinedTextField(
                        value = state.nickname,
                        onValueChange = { viewModel.onNicknameChange(it) },
                        label = { Text(stringResource(R.string.plant_details_nickname)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.plant_details_freq, state.baseWaterFreq),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = state.baseWaterFreq.toFloat(),
                        onValueChange = { viewModel.onFreqChange(it.toInt()) },
                        valueRange = 1f..30f
                    )

                    Button(
                        onClick = { viewModel.savePlant { viewModel.toggleEditMode() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(stringResource(R.string.plant_details_savePlant).uppercase())
                    }

                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow(
                                icon = Icons.Default.Category,
                                label = stringResource(R.string.plant_details_category),
                                value = state.category
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            DetailRow(
                                icon = Icons.Default.WaterDrop,
                                label = stringResource(R.string.plant_details_freqCustom),
                                value = "${state.baseWaterFreq} días"
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            DetailRow(
                                icon = Icons.Default.Schedule,
                                label = stringResource(R.string.plant_details_lastWatered),
                                value = formatDate(state.lastWatered).asString()
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            DetailRow(
                                icon = Icons.Default.Event,
                                label = stringResource(R.string.plant_details_nextWatering),
                                value = formatDate(state.nextWatering).asString()
                            )
                        }
                    }

                    WeatherAnalysisCard(
                        baseDays = state.baseWaterFreq,
                        scheduledDays = state.scheduledDays,
                        weatherTitle = state.weatherTitle,
                        weatherStatus = state.weatherStatus
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = stringResource(R.string.plant_details_careTips),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.careTips,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- COMPONENTES ----------------

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun WeatherAnalysisCard(
    baseDays: Int,
    scheduledDays: Int,
    weatherTitle: UiText?,
    weatherStatus: WeatherAnalysisStatus?
) {
    if (weatherStatus == null || weatherTitle == null) return

    val (icon, color) = when (weatherStatus) {
        WeatherAnalysisStatus.EARLY -> Icons.Default.WbSunny to MaterialTheme.colorScheme.tertiary
        WeatherAnalysisStatus.DELAYED -> Icons.Default.Info to MaterialTheme.colorScheme.secondary
        WeatherAnalysisStatus.STABLE -> Icons.Default.Info to MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = stringResource(
                        R.string.plant_details_weather_analysis,
                        weatherTitle.asString()
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = stringResource(
                        R.string.plant_details_weather_schedule,
                        baseDays,
                        scheduledDays
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
