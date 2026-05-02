package com.example.example.domain.usecase

import com.example.example.domain.repository.PaymentRepository
import javax.inject.Inject

class VoidTransactionUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(transactionId: Long, receiptId: String): Result<Unit> =
        repository.voidTransaction(transactionId, receiptId)
}
