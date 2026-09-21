package com.botaniq.ui.plantdetail

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botaniq.R
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.data.local.entities.SpeciesInfoEntity
import com.botaniq.data.repository.PlantRepository
import com.botaniq.utils.FileUtil
import com.botaniq.utils.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantFormViewModel(
    private val repository: PlantRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Estado de la UI que combina datos de Planta y Especie
    var uiState by mutableStateOf(PlantFormState())
        private set

    // Carga la lista de especies para el modo "Añadir Manual"
    init {
        val navSpeciesName: String? = savedStateHandle["speciesName"]
        val navPhotoUri: String? = savedStateHandle["photoUri"]
        val navPlantId: Int = savedStateHandle["plantId"] ?: 0

        viewModelScope.launch {
            val speciesList = repository.getAllSpecies()
            uiState = uiState.copy(availableSpecies = speciesList)


            if (navPlantId > 0) {
                // Si el id de la planta es distinto de 0, se estan consultando los detalles de una planta
                loadExistingPlant(navPlantId)
            } else if (!navSpeciesName.isNullOrBlank()) {
                // Se sabe la especie, usuario viene desde la IA
                loadFromIdentification(navSpeciesName, navPhotoUri)
            } else {
                // Registro normal, no se rellena nada previamente
                setupManualAdd()
            }
        }
    }

    // VARIANTE 1. Carga desde pantalla IA
    private suspend fun loadFromIdentification(speciesName: String, photoUri: String?) {
        val speciesInfo = repository.getSpeciesInfo(speciesName)
        uiState = uiState.copy(
            speciesName = speciesName,
            photoUri = photoUri,
            commonName = speciesInfo?.commonName ?: "",
            category = speciesInfo?.category ?: "",
            careTips = speciesInfo?.careTips ?: "",
            // Si la especie existe, se sugiere su frecuencia, si no, 7 por defecto
            baseWaterFreq = speciesInfo?.defaultWateringDays ?: 7,
            isEditMode = true
        )

    }

    // VARIANTE 2: Registro Manual
    fun setupManualAdd() {
        uiState = uiState.copy(isEditMode = true, plantId = 0)
    }

    // VARIANTE 3: Detalles
    suspend fun loadExistingPlant(plantId: Int) {
        uiState = uiState.copy(isLoading = true)
        val plant = repository.getPlantById(plantId)

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
                isEditMode = false, // Empezamos en modo detalles
                isLoading = false
            )

            analyzeWeather(it.baseWaterFreq, it.lastWateredDate, it.nextWateringDate)
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
        // Validacion datos
        if (uiState.nickname.isBlank() || uiState.speciesName.isNullOrBlank()) {
            uiState = uiState.copy(
                showError = true,
                errorMessage = UiText.StringResource(R.string.plant_details_blankError)
            )
            return
        }

        viewModelScope.launch {
            try {
                val plant = PlantEntity(
                    id = uiState.plantId, // Si es 0, Room inserta; si existe, actualiza
                    nickname = uiState.nickname,
                    speciesName = uiState.speciesName!!,
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
            } catch (e: Exception) {
                Log.e("ERROR", e.toString())
                uiState = uiState.copy(
                    showError = true,
                    errorMessage = UiText.StringResource(R.string.plant_details_saveError)
                )
            }
        }
    }

    fun onPhotoUriChange(newUri: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Se copia la imagen a la galeria y se actualiza
                val internalPath = FileUtil.saveImageToInternalStorage(context, newUri.toUri())

                uiState = uiState.copy(photoUri = internalPath)

            } catch (e: Exception) {
                Log.e("PlantFormViewModel", "Error al persistir la imagen localmente", e)
            }
        }
    }

    fun deletePlant(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val plant = PlantEntity(
                id = uiState.plantId,
                nickname = uiState.nickname,
                speciesName = uiState.speciesName ?: "Desconocida",
                photoUri = uiState.photoUri,
                baseWaterFreq = uiState.baseWaterFreq,
                lastWateredDate = uiState.lastWatered,
                nextWateringDate = uiState.nextWatering
            )
            // La foto se borra siempre que se elimine la planta
            withContext(NonCancellable) {
                repository.deletePlantWithImages(plant)
            }
            onSuccess()
        }
    }

    private fun analyzeWeather(baseDays: Int, lastWatered: Long, nextWatering: Long) {
        if (lastWatered == 0L || nextWatering == 0L) return

        val scheduled = ((nextWatering - lastWatered) / (1000L * 60 * 60 * 24)).toInt()

        val (status, title) = when {
            scheduled < baseDays -> WeatherAnalysisStatus.EARLY to UiText.StringResource(R.string.plant_details_status_early)
            scheduled > baseDays -> WeatherAnalysisStatus.DELAYED to UiText.StringResource(R.string.plant_details_status_delayed)
            else -> WeatherAnalysisStatus.STABLE to UiText.StringResource(R.string.plant_details_status_stable)
        }

        uiState = uiState.copy(
            scheduledDays = scheduled,
            weatherStatus = status,
            weatherTitle = title
        )
    }
}


@Immutable
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
    val isLoading: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: UiText? = null,
    val scheduledDays: Int = 0,
    val weatherTitle: UiText? = null,
    val weatherStatus: WeatherAnalysisStatus? = null
)

enum class WeatherAnalysisStatus {
    EARLY, DELAYED, STABLE
}