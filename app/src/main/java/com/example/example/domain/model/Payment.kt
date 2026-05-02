package com.example.example.domain.model

data class AuthorizationRequest(
    val amount: String
)

data class AuthorizationResponse(
    val receiptId: String,
    val statusCode: String,
    val statusDescription: String,
    val hexData: String
)

data class Transaction(
    val id: Long = 0,
    val receiptId: String,
    val statusCode: String,
    val statusDescription: String,
    val hexData: String,
    val amount: String,
    val timestamp: Long,
    val annulmentTimestamp: Long? = null,
    val annulmentStatusCode: String? = null,
    val annulmentStatusDescription: String? = null,
    val transactionStatus: String
)
