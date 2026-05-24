package com.botaniq.ui.screens


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.botaniq.R
import com.botaniq.data.UserPreferenceRepository
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.di.AppModule
import com.botaniq.ui.components.CitySelector
import com.botaniq.ui.components.DeleteDialog
import com.botaniq.ui.inventory.PlantViewModel
import com.botaniq.ui.inventory.PlantViewModelFactory
import com.botaniq.ui.theme.GreenSelectedIcon
import kotlinx.coroutines.CoroutineScope

@Composable
fun InventoryScreen(
    // Obtenemos el contexto de la aplicación
    context: Context = LocalContext.current.applicationContext,
    // Scope vinculado al ciclo de vida de esta composición
    scope: CoroutineScope = rememberCoroutineScope(),
    onPlantClick: (Int) -> Unit,
    viewModel: PlantViewModel = viewModel(
        factory = PlantViewModelFactory(
            preferencesRepository = UserPreferenceRepository(context),
            AppModule.providePlantRepository(
                context = context,
                AppModule.providePlantDao(AppModule.provideDatabase(context, scope)),
                AppModule.provideSpeciesInfoDao(AppModule.provideDatabase(context, scope)),
                weatherCacheDao = AppModule.provideWeatherCacheDao(
                    AppModule.provideDatabase(
                        context,
                        scope
                    )
                ),
                weatherApi = AppModule.provideWeatherApi()
            )
        )
    )
) {
    // Se consigue el estado de las plantas de forma segura para el ciclo de vida
    val plants by viewModel.plantsState.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    // Seleccion multiple de plantas y dialogo de borrado
    var selectedPlantIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteDialog(
            title = stringResource(R.string.dialog_delete_title_multiple),
            message = stringResource(
                R.string.dialog_delete_message_multiple,
                selectedPlantIds.size
            ),
            onConfirm = {
                viewModel.deleteSelectedPlants(selectedPlantIds.toList())
                selectedPlantIds = emptySet()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(horizontal = 30.dp)) {
            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.inventory_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(y = (-9).dp)
                ) {
                    // Texto con la ciudad seleccionada dinámicamente
                    Text(
                        text = currentCity,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    CitySelector(viewModel = viewModel)
                }
            }

            if (plants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.inventory_no_plants))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plants) { plant ->
                        val isSelected = selectedPlantIds.contains(plant.id)

                        PlantCard(
                            plant = plant,
                            isSelected = isSelected,
                            inSelectionMode = selectedPlantIds.isNotEmpty(),
                            onWaterConfirm = { viewModel.confirmWatering(plant) },
                            onClick = {
                                if (selectedPlantIds.isNotEmpty()) {
                                    selectedPlantIds =
                                        if (isSelected) selectedPlantIds - plant.id else selectedPlantIds + plant.id
                                } else {
                                    onPlantClick(plant.id)
                                }
                            },
                            onLongClick = {
                                if (!isSelected) selectedPlantIds = selectedPlantIds + plant.id
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }

        if (selectedPlantIds.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedPlantIds = emptySet() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.content_desc_cancel_selection)
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.inventory_selected_plants,
                            selectedPlantIds.size
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.content_desc_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

    }

}


@Composable
fun PlantCard(
    plant: PlantEntity,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onWaterConfirm: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // Modificador para la carta en funcion de si esta seleccionada o no
    val cardModifier = Modifier
        .fillMaxWidth()
        .height(250.dp)
        .then(
            if (isSelected) Modifier.border(
                3.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(10.dp)
            ) else Modifier
        )

    // Box para que el botón flote sobre la tarjeta
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 10.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {

        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                // Imagen de la planta
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            plant.photoUri ?: R.drawable.botaniq_launcher_foreground
                        ) // Si no hay foto, usa el icono de la app
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(
                        R.string.content_desc_plant_image,
                        plant.nickname
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp
                            )
                        ),
                    contentScale = ContentScale.Crop // La imagen llena el espacio sin deformarse
                )
                // Fila con informacion de la planta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = plant.nickname,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = plant.speciesName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Botón de riego
        if (!inSelectionMode) {
            IconButton(
                onClick = onWaterConfirm,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-8).dp, y = (8).dp)
                    .size(78.dp)
                    .background(
                        color = if (plant.nextWateringDate > System.currentTimeMillis())
                            GreenSelectedIcon else Color(0xFFE57373),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_inventory_water_drop),
                    contentDescription = stringResource(R.string.content_desc_confirm_watering),
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
