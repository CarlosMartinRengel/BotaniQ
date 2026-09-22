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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.CoroutineScope

private val PlantCardScrim = Brush.verticalGradient(
    colorStops = arrayOf(
        0f to Color.Transparent,
        1f to Color.Black.copy(alpha = 0.65f)
    )
)

@Composable
fun InventoryScreen(
    context: Context = LocalContext.current.applicationContext,
    scope: CoroutineScope = rememberCoroutineScope(),
    onPlantClick: (Int) -> Unit,
    onNavigateToAdd: () -> Unit,
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
    val plants by viewModel.plantsState.collectAsState()

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
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.inventory_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.inventory_plant_count,
                            plants.size,
                            plants.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CitySelector(viewModel = viewModel)
            }

            if (plants.isEmpty()) {
                EmptyInventory(onNavigateToAdd = onNavigateToAdd)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    items(plants, key = { it.id }) { plant ->
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
                }
            }
        }

        if (selectedPlantIds.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        style = MaterialTheme.typography.titleMedium,
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
fun EmptyInventory(onNavigateToAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.inventory_no_plants),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToAdd) {
            Text(stringResource(R.string.inventory_add_first))
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
    val shape = MaterialTheme.shapes.large
    val isWatered = plant.nextWateringDate > System.currentTimeMillis()
    val waterColor =
        if (isWatered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    val context = LocalContext.current
    val imageRequest = remember(plant.photoUri) {
        ImageRequest.Builder(context)
            .data(plant.photoUri ?: R.drawable.botaniq_launcher_foreground)
            .placeholder(R.drawable.botaniq_launcher_foreground)
            .error(R.drawable.botaniq_launcher_foreground)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = stringResource(
                        R.string.content_desc_plant_image,
                        plant.nickname
                    ),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .align(Alignment.BottomCenter)
                        .background(PlantCardScrim)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = plant.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = plant.speciesName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, shape)
            )
        }

        if (!inSelectionMode) {
            IconButton(
                onClick = onWaterConfirm,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(44.dp)
                    .background(waterColor, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_inventory_water_drop),
                    contentDescription = stringResource(R.string.content_desc_confirm_watering),
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
