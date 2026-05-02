package com.example.example.domain.usecase

import com.example.example.domain.model.Transaction
import com.example.example.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    operator fun invoke(): Flow<List<Transaction>> = repository.getTransactions()
}
