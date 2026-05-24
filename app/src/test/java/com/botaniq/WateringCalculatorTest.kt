package com.botaniq

import com.botaniq.data.repository.PlantRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class WateringCalculatorTest {

    @Test
    fun weatherTest() {
        val baseDays = 7
        val highTemp = 32.0 // -0.3
        val lowHum = 20.0   // -0.15

        val resultado = PlantRepository.calculateDynamicDays(baseDays, highTemp, lowHum)

        // 7 * 0.55 = 3.85
        assertEquals(3.85, resultado, 0.01)
    }

    @Test
    fun coldWeatherTest() {
        val diasBase = 7
        val lowTemp = 10.0 // +0.2
        val highHum = 80.0 // +0.2

        val resultado = PlantRepository.calculateDynamicDays(diasBase, lowTemp, highHum)

        // 7 * 1.4 = 9.8
        assertEquals(9.8, resultado, 0.01)
    }

    @Test
    fun neutralWeatherTest() {
        val baseDays = 7
        val neutralTemp = 16.0
        val neutralHum = 50.0

        val resultado = PlantRepository.calculateDynamicDays(baseDays, neutralTemp, neutralHum)


        assertEquals(7.0, resultado, 0.01)
    }

    @Test
    fun extremeHotWeatherTest() {
        val baseDays = 7
        val extremeTemp = 45.0 // -0.3
        val extremeLowHum = 10.0 // -0.15

        val resultado = PlantRepository.calculateDynamicDays(baseDays, extremeTemp, extremeLowHum)

        assertEquals(3.85, resultado, 0.01)
    }

    @Test
    fun extremeColdWeatherTest() {
        val baseDays = 7
        val extremeColdTemp = 5.0 // +0.3
        val extremeHighHum = 95.0 // +0.2

        val resultado =
            PlantRepository.calculateDynamicDays(baseDays, extremeColdTemp, extremeHighHum)

        // 7 * 1.5 = 10.5
        assertEquals(10.5, resultado, 0.01)
    }
}