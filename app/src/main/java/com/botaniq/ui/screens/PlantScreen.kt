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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Color
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
import com.botaniq.ui.components.SpeciesDropdown
import com.botaniq.ui.plantdetail.PlantFormViewModel
import com.botaniq.ui.theme.GreenSelectedIcon
import com.botaniq.utils.formatDate

@Composable
fun PlantScreen(
    plantId: Int,
    speciesName: String?,
    photoUri: String?,
    onBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    navController: NavHostController,
    viewModel: PlantFormViewModel
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    val currentEntry by navController.currentBackStackEntryAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val returnedUriState = currentEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("returnedPhotoUri", null)
        ?.collectAsState()

    val returnedUri = returnedUriState?.value

    if (showDeleteConfirmDialog) {
        DeleteDialog(
            title = stringResource(R.string.dialog_delete_title_single),
            message = stringResource(R.string.dialog_delete_message_single, state.nickname),
            onConfirm = {
                viewModel.deletePlant(onSuccess = onBack)
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    LaunchedEffect(returnedUri) {
        returnedUri?.let { uri ->
            if (uri.isNotEmpty()) {
                viewModel.onPhotoUriChange(uri, context)

                // Limpiar la caché del savedStateHandle de manera segura
                currentEntry?.savedStateHandle?.set("returnedPhotoUri", null)
            }
        }
    }

//    // Lógica de inicialización según la variación
//    LaunchedEffect(plantId) {
//        if (plantId != 0 && viewModel.uiState.plantId == 0) {
//            viewModel.loadExistingPlant(plantId)
//        } else if (plantId == 0 && viewModel.uiState.plantId == 0) {
//            viewModel.setupManualAdd()
//        }
//    }

    Scaffold(
        topBar = {
            // Barra superior (Barra de navegación)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
                Text(
                    text = when {
                        plantId == 0 -> stringResource(R.string.plant_details_topBar_newPlant)
                        state.isEditMode -> stringResource(R.string.plant_details_topBar_editPlant)
                        else -> stringResource(R.string.plant_details_topBar_details)
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                if (plantId != 0) {
                    Row {
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(
                                if (state.isEditMode) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = "Acción"
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar Planta",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
//                    .then(
//                        if (state.isEditMode) Modifier.clickable { onNavigateToCamera() }
//                        else Modifier
//                    )
            ) {
                AsyncImage(
                    model = state.photoUri ?: R.drawable.ic_launcher_foreground,
                    contentDescription = "Foto de la planta",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (state.isEditMode && plantId != 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(Color.Black, CircleShape)
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar foto",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onNavigateToCamera() }
                        )
                    }
                }

                if (state.isEditMode && plantId == 0 && state.photoUri == null) {
                    Button(
                        onClick = {
                            onNavigateToCamera()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(stringResource(R.string.plant_details_addPhoto))
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (state.isEditMode) {
                    // VARIANTES 1 y 2: Formulario
                    OutlinedTextField(
                        value = state.nickname,
                        onValueChange = { viewModel.onNicknameChange(it) },
                        label = { Text(stringResource(R.string.plant_details_nickname)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.nickname.isBlank() && state.showError,
                        supportingText = {
                            if (state.nickname.isBlank() && state.showError) {
                                Text(
                                    text = stringResource(R.string.plant_details_required),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    if (state.showError && state.errorMessage != null) {
                        Text(
                            text = stringResource(state.errorMessage),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (plantId == 0 && speciesName == null) {
                        // VARIANTE 2: Dropdown de especies
                        SpeciesDropdown(
                            options = state.availableSpecies,
                            selected = state.speciesName,
                            isError = state.speciesName.isNullOrBlank() && state.showError,
                            onSelected = { viewModel.onSpeciesChange(it) }
                        )
                    } else {
                        // VARIANTE 1: Especie bloqueada
                        Text(
                            text = stringResource(
                                R.string.plant_details_species_label,
                                state.speciesName ?: ""
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }

                    // Selector de frecuencia (Placeholder -> 7)
                    Text(
                        text = stringResource(R.string.plant_details_freq, state.baseWaterFreq),
                        style = MaterialTheme.typography.bodyMedium // O el estilo que prefieras
                    )
                    Slider(
                        value = state.baseWaterFreq.toFloat(),
                        onValueChange = { viewModel.onFreqChange(it.toInt()) },
                        valueRange = 1f..30f
                    )

                    Button(
                        onClick = { viewModel.savePlant(onBack) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)

                    ) {
                        Text(stringResource(R.string.plant_details_savePlant).uppercase())
                    }

                } else {
                    // VARIANTE 3: Detalles
                    Text(state.nickname, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${state.commonName} (${state.speciesName})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )

                    DetailRow(stringResource(R.string.plant_details_category), state.category)
                    DetailRow(
                        stringResource(R.string.plant_details_freqCustom),
                        "${state.baseWaterFreq} días"
                    )
                    DetailRow(
                        stringResource(R.string.plant_details_lastWatered),
                        formatDate(state.lastWatered)
                    )
                    DetailRow(
                        stringResource(R.string.plant_details_nextWatering),
                        formatDate(state.nextWatering)
                    )
                    WeatherAnalysisCard(
                        baseDays = state.baseWaterFreq,
                        lastWatered = state.lastWatered,
                        nextWatering = state.nextWatering
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.plant_details_careTips),
                        fontWeight = FontWeight.Bold
                    )
                    Text(state.careTips)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun WeatherAnalysisCard(baseDays: Int, lastWatered: Long, nextWatering: Long) {
    // Si no se ha regado nunca, no se muestra nada
    if (lastWatered == 0L || nextWatering == 0L) return

    val scheduledDays = ((nextWatering - lastWatered) / (1000 * 60 * 60 * 24)).toInt()

    val (icon, title, color) = when {
        scheduledDays < baseDays -> {
            Triple(
                Icons.Default.WbSunny,
                "Riego adelantado",
                Color(0xFFE57373)
            )
        }

        scheduledDays > baseDays -> {
            Triple(
                Icons.Default.Info,
                "Riego pospuesto",
                Color(0xFF64B5F6)
            )
        }

        else -> {
            Triple(
                Icons.Default.Info,
                "Clima estable",
                GreenSelectedIcon
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Análisis del Clima: $title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "Frecuencia base: $baseDays días\nProgramado por la IA: $scheduledDays días",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}