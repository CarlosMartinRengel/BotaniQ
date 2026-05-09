package com.botaniq.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.data.repository.PlantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlantViewModel(private val repository: PlantRepository) : ViewModel() {

    /**
     * Se conecta al repositorio para obtener la lista de plantas,permitiendo que la UI se modifique
     * de manera automatica en caso de cambios gracias a StateFlow.
     */
    val plantsState: StateFlow<List<PlantEntity>> = repository.allPlants
        .stateIn(
            scope = viewModelScope, //Vinculado al ViewModel. Si se cierra la pantalla, se cierra el scope
            started = SharingStarted.WhileSubscribed(5000), //Si se cambia de app, el flujo no se corta inmediatamente
            initialValue = emptyList() //Valor inicial vacio mientras se consulta la info
        )

    fun confirmWatering(plant: PlantEntity) {
        viewModelScope.launch { // Se hace en un hilo secundario -> App sigue funcionando mientras el sistema hace los cálculos
            repository.confirmWatering(plant, "Salamanca")
        }
    }

}