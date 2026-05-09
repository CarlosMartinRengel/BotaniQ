package com.botaniq.ui.screens


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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.botaniq.R
import com.botaniq.ui.components.SpeciesDropdown
import com.botaniq.ui.plantdetail.PlantFormViewModel
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

    val navBackStackEntry = navController.currentBackStackEntry
    val returnedUri = navBackStackEntry?.savedStateHandle?.get<String>("returnedPhotoUri")

    LaunchedEffect(returnedUri) {
        if (returnedUri != null) {
            viewModel.onPhotoUriChange(returnedUri)
            // Limpiamos el valor para que no se repita al rotar la pantalla
            navBackStackEntry.savedStateHandle.remove<String>("returnedPhotoUri")
        }
    }

    // Lógica de inicialización según la variación
    LaunchedEffect(Unit) {
        when {
            plantId != 0 -> viewModel.loadExistingPlant(plantId)
            speciesName != null && photoUri != null -> viewModel.loadFromIdentification(
                speciesName,
                photoUri
            )

            else -> viewModel.setupManualAdd()
        }
    }

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
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            if (state.isEditMode) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = "Acción"
                        )
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
            // 1. FOTO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = state.photoUri ?: R.drawable.ic_launcher_foreground,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (plantId == 0 && speciesName == null) {
                        // VARIANTE 2: Dropdown de especies
                        SpeciesDropdown(
                            options = state.availableSpecies,
                            selected = state.speciesName,
                            onSelected = { viewModel.onSpeciesChange(it) }
                        )
                    } else {
                        // VARIANTE 1: Especie bloqueada
                        Text(
                            text = stringResource(
                                R.string.plant_details_species_label,
                                state.speciesName ?: ""
                            ),
                            style = MaterialTheme.typography.bodyLarge
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

// TODO Poder editar la foto desde la pantalla de edicion