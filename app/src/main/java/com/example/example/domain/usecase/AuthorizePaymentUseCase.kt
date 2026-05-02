package com.example.example.domain.usecase

import com.example.example.domain.model.AuthorizationResponse
import com.example.example.domain.repository.PaymentRepository
import javax.inject.Inject

class AuthorizePaymentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(amount: String): Result<AuthorizationResponse> =
        repository.authorize(amount)
}
