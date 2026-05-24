package com.botaniq.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botaniq.data.UserPreferenceRepository
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlantViewModel(
    private val preferencesRepository: UserPreferenceRepository,
    private val repository: PlantRepository
) : ViewModel() {

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

    private val _currentCity = MutableStateFlow("Salamanca")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    val availableCities = listOf(
        "Alicante",
        "Barcelona",
        "Bilbao",
        "Córdoba",
        "Gijón",
        "Las Palmas de Gran Canaria",
        "Madrid",
        "Málaga",
        "Murcia",
        "Palma de Mallorca",
        "Salamanca",
        "Sevilla",
        "Valencia",
        "Valladolid",
        "Vigo",
        "Zamora",
        "Zaragoza"
    ).sorted()

    init {
        viewModelScope.launch {

            preferencesRepository.selectedCityFlow.collectLatest { city ->
                _currentCity.value = city
            }
        }
        viewModelScope.launch {
            repository.cleanImages()
        }
    }

    fun updateCity(newCity: String) {
        viewModelScope.launch {
            preferencesRepository.saveSelectedCity(newCity)
        }
    }

    fun confirmWatering(plant: PlantEntity) {
        viewModelScope.launch { // Se hace en un hilo secundario -> App sigue funcionando mientras el sistema hace los cálculos
            repository.confirmWatering(plant, currentCity.value)

        }
    }

    fun deletePlant(plant: PlantEntity) {
        viewModelScope.launch {
            repository.deletePlantWithImages(plant)
        }
    }

    fun deleteSelectedPlants(plantIds: List<Int>) {
        viewModelScope.launch {
            val currentPlants = plantsState.value
            val plantsToDelete = currentPlants.filter { it.id in plantIds }

            plantsToDelete.forEach { plant ->
                repository.deletePlantWithImages(plant)
            }
        }
    }

}