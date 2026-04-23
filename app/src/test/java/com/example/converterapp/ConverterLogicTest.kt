package com.example.converterapp

import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterLogicTest {

    @Test
    fun testCurrencyConversion() {
        val amount = 100.0
        val fromRate = 75.0
        val toRate = 1.0
        val expected = 7500.0
        val actual = amount * fromRate / toRate
        assertEquals(expected, actual, 0.001)
    }
}