package com.botaniq

import com.botaniq.ui.plantdetail.PlantFormState
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

        // Simulamos la lógica que tienes en tu savePlant
        if (state.nickname.isBlank()) {
            state = state.copy(showError = true, errorMessage = 12345)
        }

        assertTrue(state.showError)
        assertEquals(12345, state.errorMessage)
    }
}