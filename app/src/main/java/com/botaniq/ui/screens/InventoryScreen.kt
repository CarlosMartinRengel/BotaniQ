package com.botaniq.ui.screens


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.botaniq.R
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.di.AppModule
import com.botaniq.ui.inventory.PlantViewModel
import com.botaniq.ui.inventory.PlantViewModelFactory
import com.botaniq.ui.theme.GreenSelectedIcon
import kotlinx.coroutines.CoroutineScope

@Composable
fun InventoryScreen(
    // Obtenemos el contexto de la aplicación
    context: Context = LocalContext.current,
    // Creamos un scope vinculado al ciclo de vida de esta composición
    scope: CoroutineScope = rememberCoroutineScope(),
    onPlantClick: (Int) -> Unit,
    viewModel: PlantViewModel = viewModel(
        factory = PlantViewModelFactory(
            AppModule.providePlantRepository(
                AppModule.providePlantDao(AppModule.provideDatabase(context, scope)),
                AppModule.provideSpeciesInfoDao(AppModule.provideDatabase(context, scope))
            )
        )
    )
) {
    // Se consigue el estado de las plantas de forma segura para el ciclo de vida
    val plants by viewModel.plantsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Text(
            text = "TUS PLANTAS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (plants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes plantas. ¡Añade la primera!")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plants) { plant ->
                    PlantCard(
                        plant = plant,
                        onWaterConfirm = { viewModel.confirmWatering(plant) },
                        onClick = { onPlantClick(plant.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlantCard(
    plant: PlantEntity,
    onWaterConfirm: () -> Unit,
    onClick: () -> Unit
) {
    // Box para que el botón flote sobre la tarjeta
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 10.dp)
            .clickable { onClick() }
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                // Imagen de la planta
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            plant.photoUri ?: R.drawable.ic_launcher_background
                        ) // Si no hay foto, usa el icono de la app
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen de ${plant.nickname}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp
                            )
                        ), // Redondeo arriba
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
                painter = painterResource(id = R.drawable.ic_bottom_plant),
                contentDescription = "Confirmar Riego",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}