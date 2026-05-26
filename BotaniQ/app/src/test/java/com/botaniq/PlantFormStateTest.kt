package com.botaniq

import com.botaniq.ui.plantdetail.PlantFormState
import com.botaniq.utils.UiText // Importamos la clase mágica
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantFormStateTest {

    @Test
    fun `Un estado recien creado no debe mostrar errores`() {
        val state = PlantFormState()
        assertFalse(state.showError)
    }

    @Test
    fun `Simulacion de validacion fallida activa la bandera de error`() {
        var state = PlantFormState(nickname = "")
        val error = UiText.StringResource(12345)

        if (state.nickname.isBlank()) {
            state = state.copy(showError = true, errorMessage = error)
        }

        assertTrue(state.showError)
        assertEquals(error, state.errorMessage)
    }
}