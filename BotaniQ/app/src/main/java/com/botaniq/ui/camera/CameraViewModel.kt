package com.botaniq.ui.camera

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botaniq.R
import com.botaniq.data.tensorflow.TFLiteAnalyzer
import com.botaniq.utils.UiText
import kotlinx.coroutines.launch

enum class ScannerMode(val titleRes: Int) {
    RECOGNITION(R.string.camera_mode_recognition),
    DIAGNOSTIC(R.string.camera_mode_diagnosis)
}

@Immutable
data class CameraUIState(
    val selectedMode: ScannerMode = ScannerMode.RECOGNITION,
    val capturedImageUri: Uri? = null,
    val isPermissionGranted: Boolean = false,
    val isProcessing: Boolean = false,
    val errorMsg: UiText? = null,
    val recognizedSpecies: UiText? = null,
    val recognitionConfidence: Float? = null
)

class CameraViewModel(private val tfLiteAnalyzer: TFLiteAnalyzer) : ViewModel() {
    var uiState by mutableStateOf(CameraUIState())
        private set

    fun setScannerMode(mode: ScannerMode) {
        uiState = uiState.copy(selectedMode = mode)
    }

    fun onPermissionResult(isGranted: Boolean) {
        uiState = uiState.copy(isPermissionGranted = isGranted)
    }

    // Se acepta la foto
    fun onImageCaptured(uri: Uri) {
        uiState = uiState.copy(capturedImageUri = uri)
    }

    // Se repite la foto
    fun clearCapturedImage() {
        uiState = uiState.copy(
            capturedImageUri = null,
            recognizedSpecies = null,
            recognitionConfidence = null,
            errorMsg = null
        )
    }

    fun identifyPlant(bitmap: Bitmap) {
        uiState = uiState.copy(isProcessing = true, errorMsg = null)
        viewModelScope.launch {

            val result = tfLiteAnalyzer.analyzeSpecies(bitmap)
            Log.e(
                "IA_BOTANIQ",
                "La IA ha detectado: ${result.speciesName} con una seguridad de: ${result.confidence}"
            )

            // ENtra en la opcion de "Fondo" es decir no hay planta o no se reconoce ninguna
            if (result.speciesName.equals("Fondo no planta", ignoreCase = true)) {
                uiState = uiState.copy(
                    isProcessing = false,
                    recognizedSpecies = null,
                    recognitionConfidence = null,
                    errorMsg = UiText.StringResource(R.string.ia_mode_background)
                )
                return@launch
            }

            if (result.confidence < 0.5f) {
                uiState = uiState.copy(
                    isProcessing = false,
                    recognizedSpecies = null,
                    recognitionConfidence = null,
                    errorMsg = UiText.StringResource(R.string.ia_mode_lowConfidence)
                )
                return@launch
            }

            uiState = uiState.copy(
                isProcessing = false,
                recognizedSpecies = UiText.DynamicString(result.speciesName),
                recognitionConfidence = result.confidence,
                errorMsg = null
            )
        }
    }

    fun diagnosePlant(bitmap: Bitmap) {
        uiState = uiState.copy(isProcessing = true, errorMsg = null)

        viewModelScope.launch {
            val result = tfLiteAnalyzer.analyzeHealth(bitmap)
            Log.e(
                "IA_DIAGNOSTICO",
                "La IA detecta: ${result.anomalyDescription} con confianza: ${result.confidence}"
            )

            // Clase de rechazo
            if (result.anomalyDescription.contains("invalida", ignoreCase = true)) {
                uiState = uiState.copy(
                    isProcessing = false,
                    recognizedSpecies = null,
                    recognitionConfidence = null,
                    errorMsg = UiText.StringResource(R.string.ia_mode_background)
                )
                return@launch
            }

            // Poca confianza
            if (result.confidence < 0.5f) {
                uiState = uiState.copy(
                    isProcessing = false,
                    recognizedSpecies = null,
                    recognitionConfidence = null,
                    errorMsg = UiText.StringResource(R.string.ia_mode_lowConfidence_diagnostic)
                )
                return@launch
            }

            // Se detecta algo
            val statusText = if (result.isHealthy) {
                UiText.StringResource(R.string.ia_status_healthy)
            } else {
                UiText.StringResource(R.string.ia_status_anomaly, result.anomalyDescription)
            }

            uiState = uiState.copy(
                isProcessing = false,
                recognizedSpecies = statusText,
                recognitionConfidence = result.confidence,
                errorMsg = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        tfLiteAnalyzer.close()
    }

}