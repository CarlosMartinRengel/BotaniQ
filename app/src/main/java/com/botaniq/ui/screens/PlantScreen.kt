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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.botaniq.R
import com.botaniq.di.AppModule
import com.botaniq.ui.components.SpeciesDropdown
import com.botaniq.ui.plantdetail.PlantFormViewModel
import com.botaniq.ui.plantdetail.PlantFormViewModelFactory
import com.botaniq.utils.formatDate

@Composable
fun PlantScreen(
    plantId: Int,
    speciesName: String?,
    photoUri: String?,
    onBack: () -> Unit,
    viewModel: PlantFormViewModel = viewModel(
        factory = PlantFormViewModelFactory(
            AppModule.providePlantRepository(
                AppModule.providePlantDao(
                    AppModule.provideDatabase(
                        LocalContext.current,
                        rememberCoroutineScope()
                    )
                ),
                AppModule.provideSpeciesInfoDao(
                    AppModule.provideDatabase(
                        LocalContext.current,
                        rememberCoroutineScope()
                    )
                )
            )
        )
    ),
) {
    val state = viewModel.uiState

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
                    .padding(16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
                Text(
                    text = when {
                        plantId == 0 -> "Nueva Planta"
                        state.isEditMode -> "Editar Planta"
                        else -> "Detalles"
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
                    // Botón para añadir foto
                    Button(
                        onClick = { /*TODO Lógica cámara */ },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text("Añadir Foto")
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (state.isEditMode) {
                    // VARIANTES 1 y 2: Formulario
                    OutlinedTextField(
                        value = state.nickname,
                        onValueChange = { viewModel.onNicknameChange(it) },
                        label = { Text("Apodo de la planta") },
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
                            "Especie: ${state.speciesName}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Selector de frecuencia (Placeholder -> 7)
                    Text("Frecuencia de riego: ${state.baseWaterFreq} días")
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
                        Text("GUARDAR PLANTA")
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

                    DetailRow("Categoría", state.category)
                    DetailRow("Frecuencia personalizada", "${state.baseWaterFreq} días")
                    DetailRow("Último riego", formatDate(state.lastWatered))
                    DetailRow("Próximo riego", formatDate(state.nextWatering))

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Consejos de cuidado:", fontWeight = FontWeight.Bold)
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
// TODO Cambiar pantalla para que suba la barra de arriba hasta arriba
// TODO Poder editar la foto desde la pantalla de edicion