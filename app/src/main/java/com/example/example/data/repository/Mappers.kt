package com.example.example.data.repository

import com.example.example.data.local.TransactionEntity
import com.example.example.data.remote.AuthorizationResponseDto
import com.example.example.domain.model.AuthorizationResponse
import com.example.example.domain.model.Transaction

fun AuthorizationResponseDto.toDomain(): AuthorizationResponse {
    requireNotNull(receiptId) { "Respuesta sin datos" }
    requireNotNull(statusCode) { "Respuesta sin datos" }
    requireNotNull(statusDescription) { "Respuesta sin datos" }
    requireNotNull(hexData) { "Respuesta sin datos" }
    return AuthorizationResponse(
        receiptId = receiptId,
        statusCode = statusCode,
        statusDescription = statusDescription,
        hexData = hexData
    )
}

fun AuthorizationResponse.toEntity(amount: String, timestamp: Long) = TransactionEntity(
    receiptId = receiptId,
    statusCode = statusCode,
    statusDescription = statusDescription,
    hexData = hexData,
    amount = amount,
    timestamp = timestamp
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    receiptId = receiptId,
    statusCode = statusCode,
    statusDescription = statusDescription,
    hexData = hexData,
    amount = amount,
    timestamp = timestamp,
    annulmentTimestamp = annulmentTimestamp,
    annulmentStatusCode = annulmentStatusCode,
    annulmentStatusDescription = annulmentStatusDescription,
    transactionStatus = transactionStatus
)
