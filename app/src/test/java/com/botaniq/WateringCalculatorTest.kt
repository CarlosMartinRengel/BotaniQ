package com.botaniq

import com.botaniq.data.repository.PlantRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class WateringCalculatorTest {
    @Test
    fun weatherTest() {
        val baseDays = 7
        val highTemp = 32.0
        val lowHum = 20.0

        val resultado = PlantRepository.calculateDynamicDays(baseDays, highTemp, lowHum)

        // Reusltado tiene que ser MENOR que 7 días
        assertEquals(4.9, resultado, 0.1)
    }

    @Test
    fun coldWeatherTest() {
        val diasBase = 7
        val lowTemp = 14.0
        val highHum = 80.0

        val resultado = PlantRepository.calculateDynamicDays(diasBase, lowTemp, highHum)

        // Esperamos que el resultado sea MAYOR que 7 días
        assertEquals(9.1, resultado, 0.1)
    }

    @Test
    fun neutralWeatherTest() {
        val baseDays = 7
        val neutralTemp = 22.0
        val neutralHum = 50.0

        val resultado = PlantRepository.calculateDynamicDays(baseDays, neutralTemp, neutralHum)

        // El resultado debería ser igual a los días base
        assertEquals(7.0, resultado, 0.1)
    }

    @Test
    fun extremeHotWeatherTest() {
        val baseDays = 7
        val extremeTemp = 45.0
        val extremeLowHum = 10.0

        val resultado = PlantRepository.calculateDynamicDays(baseDays, extremeTemp, extremeLowHum)

        // El resultado debería ser significativamente menor que los días base
        assertEquals(4.9, resultado, 0.1)
    }

    @Test
    fun extremeColdWeatherTest() {
        val baseDays = 7
        val extremeColdTemp = 5.0
        val extremeHighHum = 95.0

        val resultado =
            PlantRepository.calculateDynamicDays(baseDays, extremeColdTemp, extremeHighHum)

        // El resultado debería ser significativamente mayor que los días base
        assertEquals(9.1, resultado, 0.1)
    }
}