package com.example.example

import com.example.example.domain.usecase.MaskFrameUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MaskFrameUseCaseTest {

    private lateinit var useCase: MaskFrameUseCase

    @Before
    fun setup() {
        useCase = MaskFrameUseCase()
    }



    @Test
    fun `frame shorter than 8 chars returns unchanged`() {
        assertEquals("1234567", useCase("1234567"))
    }

    @Test
    fun `frame of exactly 8 chars masks nothing in middle`() {
        assertEquals("12341234", useCase("12341234"))
    }

    @Test
    fun `frame of 16 chars masks middle 8`() {
        assertEquals("1234******3456", useCase("1234567890123456D1212201"))
    }

    @Test
    fun `empty frame returns empty`() {
        assertEquals("", useCase(""))
    }
}
