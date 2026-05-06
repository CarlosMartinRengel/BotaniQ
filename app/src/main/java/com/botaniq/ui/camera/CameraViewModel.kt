package com.botaniq.ui.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.botaniq.R

enum class ScannerMode(val titleRes: Int) {
    RECOGNITION(R.string.camera_mode_recognition),
    DIAGNOSTIC(R.string.camera_mode_diagnosis)
}

data class CameraUIState(
    val selectedMode: ScannerMode = ScannerMode.RECOGNITION,
    val isPermissionGranted: Boolean = false,
    val isProcessing: Boolean = false,
    val errorMsg: String? = null
)

class CameraViewModel : ViewModel() {
    var uiState by mutableStateOf(CameraUIState())
        private set

    fun setScannerMode(mode: ScannerMode) {
        uiState = uiState.copy(selectedMode = mode)
    }

    fun onPermissionResult(isGranted: Boolean) {
        uiState = uiState.copy(isPermissionGranted = isGranted)
    }
}