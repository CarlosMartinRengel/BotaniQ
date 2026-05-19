package com.botaniq

import com.botaniq.utils.formatDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Calendar

class DateUtilTest {

    @Test
    fun `formatDate devuelve texto de no registrado cuando el timestamp es 0`() {
        val result = formatDate(0L)
        assertEquals("Aún no registrado", result)
    }

    @Test
    fun `formatDate devuelve una fecha formateada correctamente con un timestamp valido`() {
        // Configuramos una fecha conocida (ej. 1 de Enero de 2026)
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 1)
        }
        val timestamp = calendar.timeInMillis

        val result = formatDate(timestamp)

        // Verificamos que el formato contiene el año
        assertNotEquals("Aún no registrado", result)
        assertEquals(true, result.contains("2026"))
    }
}