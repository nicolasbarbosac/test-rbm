package com.example.example.domain.repository

import com.example.example.domain.model.AuthorizationResponse
import com.example.example.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun authorize(amount: String): Result<AuthorizationResponse>
    suspend fun voidTransaction(transactionId: Long, receiptId: String): Result<Unit>
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun deleteAll()
}
