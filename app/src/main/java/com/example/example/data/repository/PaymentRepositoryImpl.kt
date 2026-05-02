package com.example.example.data.repository

import com.example.example.data.local.TransactionDao
import com.example.example.data.remote.ApiConstants
import com.example.example.data.remote.AuthorizationRequestDto
import com.example.example.data.remote.PaymentApiService
import com.example.example.data.remote.VoidRequestDto
import com.example.example.domain.HexDecoder
import com.example.example.domain.StringMasker
import com.example.example.domain.model.AuthorizationResponse
import com.example.example.domain.model.Transaction
import com.example.example.domain.repository.PaymentRepository
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymentApiService,
    private val dao: TransactionDao,
    private val masker: StringMasker,
    private val hexDecoder: HexDecoder
) : PaymentRepository {

    override suspend fun authorize(amount: String): Result<AuthorizationResponse> = try {
        val response = api.authorize(ApiConstants.AUTH_HEADER, AuthorizationRequestDto(amount))
        val domain = response.toDomain()
        val decoded = hexDecoder.decode(domain.hexData)
        val maskedHex = masker.mask(decoded)
        if (domain.statusCode == "00") {
            val entity = domain.toEntity(amount, System.currentTimeMillis())
            dao.insert(entity.copy(hexData = maskedHex))
        }
        Result.success(domain.copy(hexData = maskedHex))
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    override suspend fun voidTransaction(transactionId: Long, receiptId: String): Result<Unit> = try {
        val response = api.voidTransaction(ApiConstants.AUTH_HEADER, VoidRequestDto(receiptId))
        dao.updateAnnulment(
            id = transactionId,
            voidTimestamp = System.currentTimeMillis(),
            voidStatusCode = response.statusCode.orEmpty(),
            voidStatusDescription = response.statusDescription.orEmpty()
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    override fun getTransactions(): Flow<List<Transaction>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun deleteAll() = dao.deleteAll()

    private fun mapException(e: Exception): Exception {
        val isJsonError = e is JsonSyntaxException || e is MalformedJsonException
                || e.cause is JsonSyntaxException || e.cause is MalformedJsonException
        val isNetworkError = e is IOException && !isJsonError
        val message = when {
            isJsonError -> "Respuesta sin datos"
            isNetworkError -> "Error de red"
            else -> e.message
        }
        return IllegalStateException(message)
    }
}
