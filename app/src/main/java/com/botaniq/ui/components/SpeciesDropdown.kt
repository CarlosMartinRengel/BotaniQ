package com.botaniq.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.botaniq.data.local.entities.SpeciesInfoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesDropdown(
    options: List<SpeciesInfoEntity>,
    selected: String?,
    onSelected: (SpeciesInfoEntity) -> Unit
) {
    // Estado para controlar si el menú está desplegado o no
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        // Campo de texto visible
        OutlinedTextField(
            value = selected ?: "Selecciona una especie",
            onValueChange = {},
            readOnly = true, // Se elige de la lista -> no se puede editar
            label = { Text("Especie") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        // El menú desplegable con las opciones
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay especies registradas") },
                    onClick = { expanded = false }
                )
            } else {
                options.forEach { species ->
                    DropdownMenuItem(
                        text = { Text(species.scientificName) },
                        onClick = {
                            onSelected(species)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

