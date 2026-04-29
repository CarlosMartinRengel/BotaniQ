package com.botaniq.ui.plantdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.botaniq.data.repository.PlantRepository

class PlantFormViewModelFactory(private val repository: PlantRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantFormViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}