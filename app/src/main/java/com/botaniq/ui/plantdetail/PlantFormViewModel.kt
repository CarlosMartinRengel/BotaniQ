package com.botaniq.ui.plantdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.data.local.entities.SpeciesInfoEntity
import com.botaniq.data.repository.PlantRepository
import kotlinx.coroutines.launch

class PlantFormViewModel(private val repository: PlantRepository) : ViewModel() {

    // Estado de la UI que combina datos de Planta y Especie
    var uiState by mutableStateOf(PlantFormState())
        private set

    // Carga la lista de especies para el modo "Añadir Manual"
    init {
        viewModelScope.launch {
            val speciesList = repository.getAllSpecies()
            uiState = uiState.copy(availableSpecies = speciesList)
        }
    }

    // VARIANTE 1. Carga desde pantalla IA
    fun loadFromIdentification(speciesName: String, photoUri: String) {
        viewModelScope.launch {
            val speciesInfo = repository.getSpeciesInfo(speciesName)
            uiState = uiState.copy(
                speciesName = speciesName,
                photoUri = photoUri,
                commonName = speciesInfo?.commonName ?: "",
                category = speciesInfo?.category ?: "",
                careTips = speciesInfo?.careTips ?: "",
                // Si la especie existe, sugerimos su frecuencia, si no, 7 por defecto
                baseWaterFreq = speciesInfo?.defaultWateringDays ?: 7,
                isEditMode = true
            )
        }
    }

    // VARIANTE 2: Registro Manual
    fun setupManualAdd() {
        uiState = uiState.copy(isEditMode = true, plantId = 0)
    }

    // VARIANTE 3: Detalles
    fun loadExistingPlant(plantId: Int) {
        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch {
            // Buscamos la planta en el flujo de todas las plantas (o podrías añadir getPlantById en Repo)
            repository.allPlants.collect { plants ->
                val plant = plants.find { it.id == plantId }
                plant?.let {
                    val speciesInfo = repository.getSpeciesInfo(it.speciesName)
                    uiState = uiState.copy(
                        plantId = it.id,
                        nickname = it.nickname,
                        speciesName = it.speciesName,
                        photoUri = it.photoUri,
                        baseWaterFreq = it.baseWaterFreq,
                        lastWatered = it.lastWateredDate,
                        nextWatering = it.nextWateringDate,
                        commonName = speciesInfo?.commonName ?: "",
                        category = speciesInfo?.category ?: "",
                        careTips = speciesInfo?.careTips ?: "",
                        isEditMode = false, // Empezamos en modo lectura (Detalle)
                        isLoading = false
                    )
                }
            }
        }
    }

    // Funciones para actualizar el estado desde el formulario
    fun onNicknameChange(newNickname: String) {
        uiState = uiState.copy(nickname = newNickname)
    }

    fun onSpeciesChange(species: SpeciesInfoEntity) {
        uiState = uiState.copy(
            speciesName = species.scientificName,
            commonName = species.commonName,
            baseWaterFreq = species.defaultWateringDays
        )
    }

    fun onFreqChange(newFreq: Int) {
        uiState = uiState.copy(baseWaterFreq = newFreq)
    }

    fun toggleEditMode() {
        uiState = uiState.copy(isEditMode = !uiState.isEditMode)
    }


    fun savePlant(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val plant = PlantEntity(
                id = uiState.plantId, // Si es 0, Room inserta; si existe, actualiza
                nickname = uiState.nickname,
                speciesName = uiState.speciesName ?: "Desconocida",
                photoUri = uiState.photoUri,
                baseWaterFreq = uiState.baseWaterFreq,
                lastWateredDate = uiState.lastWatered,
                nextWateringDate = uiState.nextWatering
            )

            if (uiState.plantId == 0) {
                repository.registerPlant(plant)
            } else {
                repository.updatePlant(plant)
            }
            onSuccess()
        }
    }
}


data class PlantFormState(
    val plantId: Int = 0,
    val nickname: String = "",
    val speciesName: String? = null,
    val photoUri: String? = null,
    val baseWaterFreq: Int = 7,
    val lastWatered: Long = 0,
    val nextWatering: Long = 0,
    val commonName: String = "",
    val category: String = "",
    val careTips: String = "",
    val availableSpecies: List<SpeciesInfoEntity> = emptyList(),
    val isEditMode: Boolean = false, // Por defecto true para registros nuevos
    val isLoading: Boolean = false
)