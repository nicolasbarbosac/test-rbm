package com.example.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptId: String,
    val statusCode: String,
    val statusDescription: String,
    val hexData: String,
    val amount: String,
    val timestamp: Long,
    val annulmentTimestamp: Long? = null,
    val annulmentStatusCode: String? = null,
    val annulmentStatusDescription: String? = null,
    val transactionStatus: String = TransactionType.VENTA.description
)
