package com.example.example

import com.example.example.domain.model.Transaction
import com.example.example.domain.repository.PaymentRepository
import com.example.example.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetTransactionsUseCaseTest {

    private val repository: PaymentRepository = mock()
    private val useCase = GetTransactionsUseCase(repository)

    @Test
    fun `invoke returns transactions from repository`() = runTest {
        val expected = listOf(
            Transaction(
                id = 1,
                receiptId = "8f72-4b0b-886b",
                statusCode = "00",
                statusDescription = "Aprobada",
                hexData = "AABB",
                amount = "999929",
                timestamp = 1000L,
                transactionStatus = "Venta aprobada"
            )
        )
        whenever(repository.getTransactions()).thenReturn(flowOf(expected))

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns empty list when no transactions`() = runTest {
        whenever(repository.getTransactions()).thenReturn(flowOf(emptyList()))

        val result = useCase().first()

        assertEquals(emptyList<Transaction>(), result)
    }
}
