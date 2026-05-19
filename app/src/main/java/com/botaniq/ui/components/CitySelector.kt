package com.botaniq.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.botaniq.ui.inventory.PlantViewModel

@Composable
fun CitySelector(
    viewModel: PlantViewModel,
    modifier: Modifier = Modifier
) {
    val currentCity by viewModel.currentCity.collectAsState()
    val cities = viewModel.availableCities

    // Estado local para controlar si el menú desplegable está abierto o cerrado
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        // Icono de ubicación clickable
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Cambiar ubicación"
            )
        }

        // Menú desplegable flotante
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = {
                        // Para que se sepa que ciudad está seleccionada
                        if (city == currentCity) {
                            Text(text = "$city (Actual)")
                        } else {
                            Text(text = city)
                        }
                    },
                    onClick = {
                        viewModel.updateCity(city)
                        expanded = false
                    }
                )
            }
        }
    }
}