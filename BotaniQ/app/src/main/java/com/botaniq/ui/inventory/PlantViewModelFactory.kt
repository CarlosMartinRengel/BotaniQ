package com.botaniq.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.botaniq.data.UserPreferenceRepository
import com.botaniq.data.repository.PlantRepository

class PlantViewModelFactory(
    private val preferencesRepository: UserPreferenceRepository,
    private val repository: PlantRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantViewModel(preferencesRepository, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}