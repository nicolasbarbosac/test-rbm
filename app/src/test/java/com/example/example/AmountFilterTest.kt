package com.example.example

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountFilterTest {

    private fun filterAmount(input: String): String =
        input.replace(Regex("[^0-9]"), "")

    @Test
    fun `filters letters and special characters`() {
        assertEquals("6850000", filterAmount("COP$ 68.500,00"))
    }

    @Test
    fun `keeps only digits`() {
        assertEquals("6850000", filterAmount("6850000"))
    }

    @Test
    fun `filters all non-numeric input`() {
        assertEquals("", filterAmount("ABC!@#"))
    }

    @Test
    fun `filters spaces and symbols`() {
        assertEquals("123456", filterAmount("1 2 3-4.5,6"))
    }

    @Test
    fun `empty input returns empty`() {
        assertEquals("", filterAmount(""))
    }
}
