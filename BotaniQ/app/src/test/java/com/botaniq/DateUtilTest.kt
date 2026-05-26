package com.botaniq

import com.botaniq.utils.UiText
import com.botaniq.utils.formatDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateUtilTest {

    @Test
    fun `formatDate devuelve texto de no registrado cuando el timestamp es 0`() {
        val result = formatDate(0L)

        assertTrue(result is UiText.StringResource)

        val resourceResult = result as UiText.StringResource
        assertEquals(R.string.date_not_registered, resourceResult.resId)
    }

    @Test
    fun `formatDate devuelve una fecha formateada correctamente con un timestamp valido`() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 1)
        }
        val timestamp = calendar.timeInMillis

        val result = formatDate(timestamp)

        assertTrue(result is UiText.DynamicString)

        val dateString = (result as UiText.DynamicString).value
        assertTrue(dateString.contains("2026"))
    }
}