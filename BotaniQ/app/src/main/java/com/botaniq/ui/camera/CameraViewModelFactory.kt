package com.botaniq.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.botaniq.data.tensorflow.TFLiteAnalyzer

class CameraViewModelFactory(private val analyzer: TFLiteAnalyzer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(analyzer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}