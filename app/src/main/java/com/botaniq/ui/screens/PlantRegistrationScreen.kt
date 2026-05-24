package com.botaniq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.botaniq.R
import com.botaniq.ui.components.SpeciesDropdown
import com.botaniq.ui.plantdetail.PlantFormViewModel

@Composable
fun PlantRegistrationScreen(
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

    val returnedUriState = currentEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("returnedPhotoUri", null)
        ?.collectAsState()

    val returnedUri = returnedUriState?.value

    // Lógica para capturar la foto cuando volvemos de la cámara
    LaunchedEffect(returnedUri) {
        returnedUri?.let { uri ->
            if (uri.isNotEmpty()) {
                viewModel.onPhotoUriChange(uri, context)
                currentEntry?.savedStateHandle?.set("returnedPhotoUri", null)
            }
        }
    }

    // Si viene del escáner con informacion, se carga
    LaunchedEffect(Unit) {
        viewModel.setupManualAdd()
        if (photoUri != null) viewModel.onPhotoUriChange(photoUri, context)
    }

    LaunchedEffect(state.availableSpecies) {
        if (speciesName != null && state.availableSpecies.isNotEmpty()) {
            val foundSpecies = state.availableSpecies.find { it.scientificName == speciesName }

            if (foundSpecies != null) {
                viewModel.onSpeciesChange(foundSpecies)
            }
        }
    }



    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.plant_details_topBar_newPlant),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // CAJA DE IMAGEN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = state.photoUri
                        ?: R.drawable.ic_launcher_foreground, // Cambia por placeholder si tienes
                    contentDescription = stringResource(R.string.content_desc_plant_photo),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (state.photoUri == null) {
                    Button(
                        onClick = onNavigateToCamera,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(stringResource(R.string.plant_details_addPhoto))
                    }
                } else {
                    // Botón flotante para cambiar la foto si ya hay una
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .padding(12.dp)
                            .clickable { onNavigateToCamera() }
                    ) {
                        Text(
                            stringResource(R.string.plant_details_change_photo),
                            color = Color.White
                        )
                    }
                }
            }

            // FORMULARIO
            Column(modifier = Modifier.padding(16.dp)) {
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

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown o texto dependiendo de si la IA ya lo detectó
                if (speciesName == null) {
                    SpeciesDropdown(
                        options = state.availableSpecies,
                        selected = state.speciesName,
                        isError = state.speciesName.isNullOrBlank() && state.showError,
                        onSelected = { viewModel.onSpeciesChange(it) }
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.plant_details_species_label,
                            state.speciesName ?: ""
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

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
                    onClick = { viewModel.savePlant(onBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.plant_details_savePlant).uppercase())
                }
            }
        }
    }
}