package com.botaniq

import com.botaniq.ui.camera.CameraUIState
import com.botaniq.ui.camera.ScannerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraUITest {

    @Test
    fun `El estado inicial de la camara debe ser RECOGNITION y sin errores`() {
        val initialState = CameraUIState()

        assertEquals(ScannerMode.RECOGNITION, initialState.selectedMode)
        assertEquals(false, initialState.isProcessing)
        assertNull(initialState.errorMsg)
        assertNull(initialState.recognizedSpecies)
    }

    @Test
    fun `Al copiar el estado con un error, el mensaje debe guardarse correctamente`() {
        val state = CameraUIState()
        val errorState = state.copy(errorMsg = 12345)

        assertEquals(12345, errorState.errorMsg)
        assertEquals(false, errorState.isProcessing)
    }
}