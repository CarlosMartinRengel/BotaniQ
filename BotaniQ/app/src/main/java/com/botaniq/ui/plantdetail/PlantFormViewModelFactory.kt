package com.botaniq.ui.plantdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.botaniq.data.repository.PlantRepository

class PlantFormViewModelFactory(private val repository: PlantRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()
        return PlantFormViewModel(repository, savedStateHandle) as T
    }
}