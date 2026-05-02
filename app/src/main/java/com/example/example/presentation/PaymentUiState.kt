package com.example.example.presentation

import com.example.example.domain.model.Transaction

data class PaymentUiState(
    val amount: String = "",
    val authStatus: AuthorizationStatus = AuthorizationStatus.Idle,
    val transactions: List<Transaction> = emptyList(),
    val showHistory: Boolean = false,
    val selectedTransaction: Transaction? = null,
    val isVoiding: Boolean = false
)
