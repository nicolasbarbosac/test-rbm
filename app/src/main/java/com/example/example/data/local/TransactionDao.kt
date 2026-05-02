package com.example.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET annulmentTimestamp = :voidTimestamp, annulmentStatusCode = :voidStatusCode, annulmentStatusDescription = :voidStatusDescription, transactionStatus = :status WHERE id = :id")
    suspend fun updateAnnulment(id: Long, voidTimestamp: Long, voidStatusCode: String, voidStatusDescription: String, status: String = TransactionType.ANULACION.description)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
