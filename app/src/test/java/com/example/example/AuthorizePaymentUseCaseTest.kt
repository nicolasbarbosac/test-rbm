package com.example.example

import com.example.example.domain.model.AuthorizationResponse
import com.example.example.domain.repository.PaymentRepository
import com.example.example.domain.usecase.AuthorizePaymentUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AuthorizePaymentUseCaseTest {

    private val repository: PaymentRepository = mock()
    private val useCase = AuthorizePaymentUseCase(repository)

    @Test
    fun `invoke returns success with authorization response`() = runTest {
        val expected = AuthorizationResponse(
            receiptId = "8f72-4b0b-886b",
            statusCode = "00",
            statusDescription = "Aprobada",
            hexData = "313233343536373839303132333435364431323132323031"
        )
        whenever(repository.authorize("999929")).thenReturn(Result.success(expected))

        val result = useCase("999929")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke returns failure on network error`() = runTest {
        whenever(repository.authorize("999929"))
            .thenReturn(Result.failure(RuntimeException("Connection refused")))

        val result = useCase("999929")

        assertTrue(result.isFailure)
        assertEquals("Connection refused", result.exceptionOrNull()?.message)
    }
}
